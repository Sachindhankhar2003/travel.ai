package com.citygo.app.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citygo.app.domain.model.*
import com.citygo.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val currentLocation: LatLng? = null,
    val origin: Place? = null,
    val destination: Place? = null,
    val searchQuery: String = "",
    val searchResults: List<Place> = emptyList(),
    val isSearching: Boolean = false,
    val route: RouteResult? = null,
    val isRouting: Boolean = false,
    val routeError: String? = null,
    val recentSearches: List<Place> = emptyList(),
    val searchMode: SearchMode = SearchMode.DESTINATION
)

enum class SearchMode { ORIGIN, DESTINATION }

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationRepo: LocationRepository,
    private val geocodingRepo: GeocodingRepository,
    private val routingRepo: RoutingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadCurrentLocation()
        loadRecentSearches()
        setupSearchDebounce()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQuery
                .debounce(400)
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .collect { query ->
                    _uiState.update { it.copy(isSearching = true) }
                    when (val result = geocodingRepo.searchPlaces(query)) {
                        is Result.Success -> _uiState.update {
                            it.copy(searchResults = result.data, isSearching = false)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(searchResults = emptyList(), isSearching = false)
                        }
                        is Result.Loading -> Unit
                    }
                }
        }
    }

    private fun loadCurrentLocation() {
        viewModelScope.launch {
            when (val result = locationRepo.getCurrentLocation()) {
                is Result.Success -> _uiState.update { it.copy(currentLocation = result.data) }
                else -> _uiState.update { it.copy(currentLocation = LatLng(28.6139, 77.2090)) }
            }
        }
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            geocodingRepo.getRecentSearches().collect { recent ->
                _uiState.update { it.copy(recentSearches = recent) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQuery.value = query
        if (query.isEmpty()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    fun onPlaceSelected(place: Place) {
        viewModelScope.launch {
            geocodingRepo.saveSearch(place)
            when (_uiState.value.searchMode) {
                SearchMode.DESTINATION -> {
                    _uiState.update {
                        it.copy(
                            destination = place,
                            searchQuery = "",
                            searchResults = emptyList()
                        )
                    }
                    // Auto-calculate route if we have origin
                    val origin = _uiState.value.origin
                        ?: _uiState.value.currentLocation?.let { loc ->
                            Place("Your Location", "Your Location", loc)
                        }
                    if (origin != null) calculateRoute(origin, place)
                }
                SearchMode.ORIGIN -> {
                    _uiState.update {
                        it.copy(
                            origin = place,
                            searchQuery = "",
                            searchResults = emptyList()
                        )
                    }
                    val destination = _uiState.value.destination
                    if (destination != null) calculateRoute(place, destination)
                }
            }
        }
    }

    fun setSearchMode(mode: SearchMode) {
        _uiState.update { it.copy(searchMode = mode) }
    }

    fun clearRoute() {
        _uiState.update { it.copy(route = null, origin = null, destination = null, routeError = null) }
    }

    private fun calculateRoute(origin: Place, destination: Place) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRouting = true, routeError = null) }
            when (val result = routingRepo.getRoute(origin.latLng, destination.latLng)) {
                is Result.Success -> _uiState.update {
                    it.copy(route = result.data, isRouting = false, routeError = null)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isRouting = false, routeError = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun formatDistance(meters: Double): String {
        return if (meters < 1000) "${meters.toInt()} m"
        else "${"%.1f".format(meters / 1000)} km"
    }

    fun formatDuration(seconds: Double): String {
        val minutes = (seconds / 60).toInt()
        return if (minutes < 60) "$minutes min"
        else "${minutes / 60}h ${minutes % 60}m"
    }
}
