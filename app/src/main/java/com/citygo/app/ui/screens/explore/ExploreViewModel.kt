package com.citygo.app.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citygo.app.domain.model.LatLng
import com.citygo.app.domain.model.Result
import com.citygo.app.domain.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

// ─── Domain model for a POI ───────────────────────────────────────────────────
data class Place(
    val id: Long,
    val name: String,
    val category: String,
    val latLng: LatLng,
    val address: String,
    val phone: String?,
    val website: String?,
    val openingHours: String?
)

// ─── UI State ─────────────────────────────────────────────────────────────────
data class ExploreUiState(
    val currentLocation: LatLng? = null,
    val selectedCategory: ExploreCategory = ExploreCategory.RESTAURANT,
    val places: List<Place> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false,
    val searchRadius: Int = 2000   // metres
)

enum class ExploreCategory(
    val label: String,
    val overpassKey: String,
    val overpassValue: String,
    val emoji: String
) {
    RESTAURANT(  "Restaurants",  "amenity",  "restaurant",      "🍽"),
    HOTEL(       "Hotels",       "tourism",  "hotel",           "🏨"),
    CAFE(        "Cafes",        "amenity",  "cafe",            "☕"),
    HOSPITAL(    "Hospitals",    "amenity",  "hospital",        "🏥"),
    PHARMACY(    "Pharmacy",     "amenity",  "pharmacy",        "💊"),
    ATM(         "ATMs",         "amenity",  "atm",             "🏧"),
    PETROL(      "Fuel",         "amenity",  "fuel",            "⛽"),
    PARK(        "Parks",        "leisure",  "park",            "🌳"),
    SHOPPING(    "Shopping",     "shop",     "mall",            "🛍"),
    METRO(       "Metro",        "station",  "subway_entrance", "🚇"),
    MUSEUM(      "Museums",      "tourism",  "museum",          "🏛"),
    TEMPLE(      "Temples",      "amenity",  "place_of_worship","🛕"),
}

// ─── Minimal Overpass JSON model ──────────────────────────────────────────────
@Serializable
private data class OverpassResponse(
    @SerialName("elements") val elements: List<OverpassElement> = emptyList()
)

@Serializable
private data class OverpassElement(
    @SerialName("id")   val id: Long = 0,
    @SerialName("lat")  val lat: Double? = null,
    @SerialName("lon")  val lon: Double? = null,
    @SerialName("tags") val tags: Map<String, String> = emptyMap(),
    // nodes inside a way/relation have geometry:
    @SerialName("center") val center: OverpassCenter? = null
)

@Serializable
private data class OverpassCenter(
    @SerialName("lat") val lat: Double = 0.0,
    @SerialName("lon") val lon: Double = 0.0
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val locationRepo: LocationRepository,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    init {
        loadLocation()
    }

    private fun loadLocation() {
        viewModelScope.launch {
            val loc = when (val r = locationRepo.getCurrentLocation()) {
                is Result.Success -> r.data
                else -> LatLng(28.6139, 77.2090) // Delhi centre fallback
            }
            _uiState.update { it.copy(currentLocation = loc) }
            // Auto-search initial category
            searchPlaces(loc, _uiState.value.selectedCategory)
        }
    }

    fun onCategorySelected(cat: ExploreCategory) {
        _uiState.update { it.copy(selectedCategory = cat, places = emptyList(), error = null, hasSearched = false) }
        val loc = _uiState.value.currentLocation ?: return
        searchPlaces(loc, cat)
    }

    fun onRadiusChanged(radius: Int) {
        _uiState.update { it.copy(searchRadius = radius) }
        val loc = _uiState.value.currentLocation ?: return
        searchPlaces(loc, _uiState.value.selectedCategory)
    }

    fun retry() {
        val loc = _uiState.value.currentLocation ?: return
        searchPlaces(loc, _uiState.value.selectedCategory)
    }

    // ── Overpass API query ────────────────────────────────────────────────────
    private fun searchPlaces(loc: LatLng, cat: ExploreCategory) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val radius = _uiState.value.searchRadius
            val query  = buildOverpassQuery(loc, cat, radius)
            val url    = "https://overpass-api.de/api/interpreter?data=${
                java.net.URLEncoder.encode(query, "UTF-8")
            }"

            try {
                val request  = Request.Builder().url(url)
                    .header("User-Agent", "TravelAI/1.0 (Android)").build()
                val response = okHttpClient.newCall(request).execute()
                val body     = response.body?.string() ?: ""
                if (!response.isSuccessful || body.isBlank()) throw Exception("Empty response")

                val parsed = json.decodeFromString<OverpassResponse>(body)
                val places = parsed.elements
                    .filter { it.lat != null || it.center != null }
                    .take(30)
                    .mapNotNull { el ->
                        val lat = el.lat ?: el.center?.lat ?: return@mapNotNull null
                        val lng = el.lon ?: el.center?.lon ?: return@mapNotNull null
                        val name = el.tags["name"]
                            ?: el.tags["brand"]
                            ?: cat.label.dropLast(1) // remove plural 's'

                        Place(
                            id           = el.id,
                            name         = name,
                            category     = cat.label,
                            latLng       = LatLng(lat, lng),
                            address      = buildAddress(el.tags),
                            phone        = el.tags["phone"] ?: el.tags["contact:phone"],
                            website      = el.tags["website"] ?: el.tags["contact:website"],
                            openingHours = el.tags["opening_hours"]
                        )
                    }
                    .sortedBy { distanceM(loc, it.latLng) }

                _uiState.update { it.copy(isLoading = false, places = places, hasSearched = true, error = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading   = false,
                        hasSearched = true,
                        error       = when {
                            e.message?.contains("Unable to resolve") == true ||
                            e.message?.contains("No address") == true ->
                                "No internet connection. Please check your network."
                            e.message?.contains("timed out") == true ->
                                "Request timed out. Try again."
                            else -> "Could not load places: ${e.message}"
                        }
                    )
                }
            }
        }
    }

    private fun buildOverpassQuery(loc: LatLng, cat: ExploreCategory, radius: Int): String {
        val (lat, lng) = loc
        // Special case: subway_entrance uses railway key
        val keyValue = when (cat) {
            ExploreCategory.METRO -> """node["railway"="subway_entrance"](around:$radius,$lat,$lng);"""
            else -> """
                node["${cat.overpassKey}"="${cat.overpassValue}"](around:$radius,$lat,$lng);
                way["${cat.overpassKey}"="${cat.overpassValue}"](around:$radius,$lat,$lng);
            """.trimIndent()
        }
        return """
            [out:json][timeout:20];
            (
              $keyValue
            );
            out center 30;
        """.trimIndent()
    }

    private fun buildAddress(tags: Map<String, String>): String {
        val parts = listOfNotNull(
            tags["addr:housenumber"],
            tags["addr:street"],
            tags["addr:suburb"] ?: tags["addr:neighbourhood"],
            tags["addr:city"]
        )
        return if (parts.isNotEmpty()) parts.joinToString(", ")
        else tags["addr:full"] ?: ""
    }

    fun distanceText(from: LatLng, to: LatLng): String {
        val m = distanceM(from, to)
        return if (m < 1000) "${m.toInt()} m" else "${"%.1f".format(m / 1000)} km"
    }

    private fun distanceM(a: LatLng, b: LatLng): Double {
        val earthR = 6371000.0
        val dLat   = Math.toRadians(b.lat - a.lat)
        val dLng   = Math.toRadians(b.lng - a.lng)
        val sinLat = Math.sin(dLat / 2)
        val sinLng = Math.sin(dLng / 2)
        val chord  = sinLat * sinLat +
                Math.cos(Math.toRadians(a.lat)) * Math.cos(Math.toRadians(b.lat)) * sinLng * sinLng
        return earthR * 2 * Math.atan2(Math.sqrt(chord), Math.sqrt(1 - chord))
    }
}
