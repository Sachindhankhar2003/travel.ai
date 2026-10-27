package com.citygo.app.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citygo.app.AppConstants
import com.citygo.app.data.local.dao.TripPlanDao
import com.citygo.app.data.local.entity.TripPlanEntity
import com.citygo.app.data.remote.api.GeocodingApi
import com.citygo.app.data.remote.api.PricingApi
import com.citygo.app.data.remote.api.RoutingApi
import com.citygo.app.domain.model.Result
import com.citygo.app.domain.model.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TravelMode { METRO, GOVT_BUS, PRIVATE_BUS, OWN_VEHICLE }
enum class RoomType   { BUDGET, MID, PREMIUM }

data class BudgetUiState(
    val fromCity: String = "",
    val toCity: String = "",
    val travellers: Int = 1,
    val nights: Int = 1,
    val travelMode: TravelMode = TravelMode.METRO,
    val roomType: RoomType = RoomType.MID,
    val roomCount: Int = 1,
    val dailyFoodBudget: Double = 500.0,
    val returnTrip: Boolean = false,
    val fuelCostPerKm: Double = AppConstants.DEFAULT_FUEL_COST_PER_KM,

    val isCalculating: Boolean = false,
    val error: String? = null,

    // Results
    val distanceKm: Double = 0.0,
    val travelCost: Double = 0.0,
    val stayCost: Double = 0.0,
    val foodCost: Double = 0.0,
    val totalCost: Double = 0.0,
    val perPersonCost: Double = 0.0,
    val calculationDone: Boolean = false,

    // Compare strip — costs for Metro / GovtBus / PrivateBus
    val compareCosts: Map<TravelMode, Double> = emptyMap(),

    val savedPlans: List<TripPlanEntity> = emptyList()
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val tripPlanDao: TripPlanDao,
    private val pricingApi: PricingApi,
    private val geocodingApi: GeocodingApi,
    private val routingApi: RoutingApi
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetUiState())
    val state = _state.asStateFlow()

    val savedPlans = tripPlanDao.getAllPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onFromCity(v: String)          = _state.update { it.copy(fromCity = v) }
    fun onToCity(v: String)            = _state.update { it.copy(toCity = v) }
    fun onTravellers(v: Int)           = _state.update { it.copy(travellers = v.coerceIn(1, 20)) }
    fun onNights(v: Int)               = _state.update { it.copy(nights = v.coerceIn(1, 60)) }
    fun onTravelMode(v: TravelMode)    = _state.update { it.copy(travelMode = v) }
    fun onRoomType(v: RoomType)        = _state.update { it.copy(roomType = v) }
    fun onRoomCount(v: Int)            = _state.update { it.copy(roomCount = v.coerceIn(1, 10)) }
    fun onDailyFood(v: Double)         = _state.update { it.copy(dailyFoodBudget = v) }
    fun onReturnTrip(v: Boolean)       = _state.update { it.copy(returnTrip = v) }
    fun onFuelCost(v: Double)          = _state.update { it.copy(fuelCostPerKm = v) }
    fun prefillDestination(city: String) = _state.update { it.copy(toCity = city) }
    fun clearError()                   = _state.update { it.copy(error = null) }

    fun calculate() {
        val s = _state.value
        if (s.fromCity.isBlank() || s.toCity.isBlank()) {
            _state.update { it.copy(error = "Please enter both cities") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isCalculating = true, error = null, calculationDone = false) }

            // 1. Room price from backend (with offline fallback)
            val pricePerNight = fetchRoomPrice(s.toCity, s.roomType)

            // 2. Distance from OSRM for own-vehicle and compare
            val distKm = fetchDistanceKm(s.fromCity, s.toCity)

            // 3. Travel cost for selected mode
            val travelCost = calcTravelCost(s, distKm)
            val returnMult = if (s.returnTrip) 2.0 else 1.0

            // 4. Compare costs for all three transit modes
            val compareCosts = buildCompareCosts(s, distKm)

            // 5. Stay & food
            val stayCost = pricePerNight * s.nights * s.roomCount
            val foodCost = s.dailyFoodBudget * s.nights * s.travellers
            val totalCost = (travelCost * returnMult) + stayCost + foodCost
            val perPerson = totalCost / s.travellers

            _state.update {
                it.copy(
                    isCalculating = false,
                    distanceKm = distKm,
                    travelCost = travelCost * returnMult,
                    stayCost = stayCost,
                    foodCost = foodCost,
                    totalCost = totalCost,
                    perPersonCost = perPerson,
                    compareCosts = compareCosts,
                    calculationDone = true
                )
            }
        }
    }

    fun savePlan() {
        val s = _state.value
        if (!s.calculationDone) return
        viewModelScope.launch {
            tripPlanDao.insertPlan(
                TripPlanEntity(
                    fromCity = s.fromCity,
                    toCity = s.toCity,
                    travellers = s.travellers,
                    nights = s.nights,
                    travelMode = s.travelMode.name,
                    roomType = s.roomType.name,
                    roomCount = s.roomCount,
                    dailyFoodBudget = s.dailyFoodBudget,
                    returnTrip = s.returnTrip,
                    fuelCostPerKm = s.fuelCostPerKm,
                    distanceKm = s.distanceKm,
                    travelCost = s.travelCost,
                    stayCost = s.stayCost,
                    foodCost = s.foodCost,
                    totalCost = s.totalCost,
                    perPersonCost = s.perPersonCost
                )
            )
        }
    }

    fun deletePlan(id: Long) {
        viewModelScope.launch { tripPlanDao.deletePlanById(id) }
    }

    fun sharePlanText(plan: TripPlanEntity): String = buildString {
        appendLine("🌍 Travel.ai Trip Budget")
        appendLine("${plan.fromCity} → ${plan.toCity}")
        appendLine("Travellers: ${plan.travellers}  |  ${plan.nights} nights")
        appendLine("Mode: ${plan.travelMode.replace("_", " ")}")
        appendLine("Room: ${plan.roomType} × ${plan.roomCount}")
        appendLine()
        appendLine("✈ Travel  : ₹${"%.0f".format(plan.travelCost)}")
        appendLine("🏨 Stay    : ₹${"%.0f".format(plan.stayCost)}")
        appendLine("🍽 Food    : ₹${"%.0f".format(plan.foodCost)}")
        appendLine("💰 Total   : ₹${"%.0f".format(plan.totalCost)}")
        appendLine("👤 Per person: ₹${"%.0f".format(plan.perPersonCost)}")
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private suspend fun fetchRoomPrice(city: String, roomType: RoomType): Double {
        return safeApiCall { pricingApi.getHotelPricing(city, roomType.name) }
            .getOrNull()?.pricePerNight
            ?: AppConstants.ROOM_PRICE_DEFAULTS[roomType.name]
            ?: 1500.0
    }

    private suspend fun fetchDistanceKm(from: String, to: String): Double {
        // 1. Try static lookup first for instant results
        val staticKm = KnownDistances.getKm(from, to)
        if (staticKm != null) return staticKm

        // 2. Geocode both cities via Photon, then route via OSRM
        return try {
            val fromResult = safeApiCall { geocodingApi.search(from, limit = 1) }
            val toResult   = safeApiCall { geocodingApi.search(to,   limit = 1) }

            val fromCoords = (fromResult as? Result.Success)
                ?.data?.features?.firstOrNull()?.geometry?.coordinates
            val toCoords   = (toResult   as? Result.Success)
                ?.data?.features?.firstOrNull()?.geometry?.coordinates

            if (fromCoords == null || toCoords == null || fromCoords.size < 2 || toCoords.size < 2) {
                return 0.0
            }

            // Photon returns [lon, lat]
            val fromLon = fromCoords[0]; val fromLat = fromCoords[1]
            val toLon   = toCoords[0];   val toLat   = toCoords[1]
            val coordStr = "$fromLon,$fromLat;$toLon,$toLat"

            val routeResult = safeApiCall {
                routingApi.getRoute(coordStr, overview = "false", steps = false)
            }
            val distanceM = (routeResult as? Result.Success)
                ?.data?.routes?.firstOrNull()?.distance ?: 0.0
            distanceM / 1000.0  // convert metres → km
        } catch (e: Exception) {
            0.0
        }
    }

    private fun calcTravelCost(s: BudgetUiState, distKm: Double): Double {
        return when (s.travelMode) {
            TravelMode.METRO       -> 60.0 * s.travellers   // flat metro base fare estimate
            TravelMode.GOVT_BUS    -> 30.0 * s.travellers
            TravelMode.PRIVATE_BUS -> 80.0 * s.travellers
            TravelMode.OWN_VEHICLE -> distKm * s.fuelCostPerKm
        }
    }

    private fun buildCompareCosts(s: BudgetUiState, distKm: Double): Map<TravelMode, Double> {
        val mult = if (s.returnTrip) 2.0 else 1.0
        return mapOf(
            TravelMode.METRO       to 60.0  * s.travellers * mult,
            TravelMode.GOVT_BUS    to 30.0  * s.travellers * mult,
            TravelMode.PRIVATE_BUS to 80.0  * s.travellers * mult
        )
    }

    /** Approximate straight-line road distances (km) for common city pairs. */
    private object KnownDistances {
        private val table = mapOf(
            "delhi-noida" to 22.0, "delhi-gurgaon" to 30.0,
            "delhi-faridabad" to 35.0, "delhi-ghaziabad" to 28.0,
            "delhi-agra" to 210.0, "delhi-jaipur" to 282.0,
            "delhi-chandigarh" to 250.0, "delhi-mathura" to 175.0,
            "delhi-vrindavan" to 185.0, "delhi-haridwar" to 225.0,
            "delhi-rishikesh" to 240.0, "delhi-dehradun" to 300.0,
            "delhi-shimla" to 370.0, "delhi-manali" to 560.0,
            "delhi-amritsar" to 450.0, "delhi-ludhiana" to 310.0,
            "delhi-ambala" to 205.0, "delhi-rohtak" to 75.0,
            "delhi-panipat" to 95.0, "delhi-karnal" to 130.0,
            "delhi-hisar" to 170.0, "delhi-alwar" to 160.0,
            "delhi-bharatpur" to 195.0, "delhi-ajmer" to 390.0,
            "delhi-jodhpur" to 600.0, "delhi-udaipur" to 660.0,
        )

        fun getKm(a: String, b: String): Double? {
            val ka = a.trim().lowercase().replace(" ", "-")
            val kb = b.trim().lowercase().replace(" ", "-")
            return table["$ka-$kb"] ?: table["$kb-$ka"]
        }
    }
}
