package com.citygo.app.ui.screens.transit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citygo.app.domain.model.*
import com.citygo.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Metro State ─────────────────────────────────────────────────────────────
data class MetroUiState(
    val isLoading: Boolean = false,
    val nearestStations: List<MetroStation> = emptyList(),
    val fromStation: String = "",
    val toStation: String = "",
    val fromResults: List<MetroStation> = emptyList(),
    val toResults: List<MetroStation> = emptyList(),
    val route: MetroRoute? = null,
    val error: String? = null,
    val isFromCache: Boolean = false
)

// ─── Bus State ────────────────────────────────────────────────────────────────
data class BusUiState(
    val isLoading: Boolean = false,
    val fromQuery: String = "",
    val toQuery: String = "",
    val buses: List<Bus> = emptyList(),
    val filteredBuses: List<Bus> = emptyList(),
    val selectedFilter: OperatorType = OperatorType.ALL,
    val error: String? = null,
    val hasSearched: Boolean = false,
    val isFromCache: Boolean = false
)

data class TransitUiState(
    val selectedTab: Int = 0, // 0 = Metro, 1 = Bus
    val metro: MetroUiState = MetroUiState(),
    val bus: BusUiState = BusUiState()
)

@HiltViewModel
class TransitViewModel @Inject constructor(
    private val metroRepo: MetroRepository,
    private val busRepo: BusRepository,
    private val locationRepo: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransitUiState())
    val uiState: StateFlow<TransitUiState> = _uiState.asStateFlow()

    init {
        loadNearestMetroStations()
    }

    // ─── Tab ─────────────────────────────────────────────────────────────────
    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    // ─── Metro ───────────────────────────────────────────────────────────────
    private fun loadNearestMetroStations() {
        viewModelScope.launch {
            val loc = when (val r = locationRepo.getCurrentLocation()) {
                is Result.Success -> r.data
                else -> LatLng(28.6330, 77.2195) // Rajiv Chowk default
            }
            when (val result = metroRepo.getNearestStations(loc.lat, loc.lng)) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(metro = state.metro.copy(nearestStations = result.data))
                }
                else -> Unit
            }
        }
    }

    fun onMetroFromChange(query: String) {
        _uiState.update { it.copy(metro = it.metro.copy(fromStation = query)) }
        searchMetroStations(query, isFrom = true)
    }

    fun onMetroToChange(query: String) {
        _uiState.update { it.copy(metro = it.metro.copy(toStation = query)) }
        searchMetroStations(query, isFrom = false)
    }

    private fun searchMetroStations(query: String, isFrom: Boolean) {
        if (query.length < 2) return
        viewModelScope.launch {
            when (val result = metroRepo.searchStations(query)) {
                is Result.Success -> _uiState.update { state ->
                    if (isFrom) state.copy(metro = state.metro.copy(fromResults = result.data))
                    else state.copy(metro = state.metro.copy(toResults = result.data))
                }
                else -> Unit
            }
        }
    }

    fun onMetroFromSelected(station: MetroStation) {
        _uiState.update { state ->
            state.copy(metro = state.metro.copy(
                fromStation = station.name,
                fromResults = emptyList()
            ))
        }
    }

    fun onMetroToSelected(station: MetroStation) {
        _uiState.update { state ->
            state.copy(metro = state.metro.copy(
                toStation = station.name,
                toResults = emptyList()
            ))
        }
    }

    fun searchMetroRoute() {
        val from = _uiState.value.metro.fromStation
        val to = _uiState.value.metro.toStation
        if (from.isBlank() || to.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(metro = it.metro.copy(isLoading = true, error = null)) }
            when (val result = metroRepo.getRoute(from, to)) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(metro = state.metro.copy(
                        isLoading = false,
                        route = result.data,
                        isFromCache = result.data.isFromCache
                    ))
                }
                is Result.Error -> _uiState.update { state ->
                    state.copy(metro = state.metro.copy(
                        isLoading = false,
                        error = result.message
                    ))
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun swapMetroStations() {
        _uiState.update { state ->
            val metro = state.metro
            state.copy(metro = metro.copy(
                fromStation = metro.toStation,
                toStation = metro.fromStation,
                route = null
            ))
        }
    }

    // ─── Bus ──────────────────────────────────────────────────────────────────
    fun onBusFromChange(query: String) {
        _uiState.update { it.copy(bus = it.bus.copy(fromQuery = query)) }
    }

    fun onBusToChange(query: String) {
        _uiState.update { it.copy(bus = it.bus.copy(toQuery = query)) }
    }

    fun searchBuses() {
        val from = _uiState.value.bus.fromQuery
        val to = _uiState.value.bus.toQuery
        if (from.isBlank() || to.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(bus = it.bus.copy(isLoading = true, error = null)) }
            val type = when (_uiState.value.bus.selectedFilter) {
                OperatorType.GOVERNMENT -> "govt"
                OperatorType.PRIVATE -> "private"
                OperatorType.ALL -> "all"
            }
            when (val result = busRepo.searchBuses(from, to, type)) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(bus = state.bus.copy(
                        isLoading = false,
                        buses = result.data,
                        filteredBuses = result.data,
                        hasSearched = true
                    ))
                }
                is Result.Error -> _uiState.update { state ->
                    state.copy(bus = state.bus.copy(
                        isLoading = false,
                        error = result.message,
                        hasSearched = true
                    ))
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onBusFilterChange(filter: OperatorType) {
        _uiState.update { state ->
            val filtered = when (filter) {
                OperatorType.ALL -> state.bus.buses
                OperatorType.GOVERNMENT -> state.bus.buses.filter { it.operatorType == OperatorType.GOVERNMENT }
                OperatorType.PRIVATE -> state.bus.buses.filter { it.operatorType == OperatorType.PRIVATE }
            }
            state.copy(bus = state.bus.copy(selectedFilter = filter, filteredBuses = filtered))
        }
    }
}
