package com.citygo.app.ui.screens.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citygo.app.AppConstants
import com.citygo.app.domain.model.*
import com.citygo.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val weather: Weather? = null,
    val weatherError: String? = null,
    val currentLocation: LatLng? = null,
    val currentCity: City? = null,
    val isOffline: Boolean = false,
    val showCityPicker: Boolean = false,
    val locationPermissionGranted: Boolean = false,
    val tempUnit: String = "C"  // "C" | "F"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherRepo: WeatherRepository,
    private val geocodingRepo: GeocodingRepository,
    private val locationRepo: LocationRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeTempUnit()
        checkPermissionAndLoad()
    }

    private fun observeTempUnit() {
        val unitKey = stringPreferencesKey(AppConstants.PREF_TEMP_UNIT)
        viewModelScope.launch {
            dataStore.data.map { it[unitKey] ?: "C" }.distinctUntilChanged().collect { unit ->
                _uiState.update { it.copy(tempUnit = unit) }
            }
        }
    }

    fun checkPermissionAndLoad() {
        val hasPermission = locationRepo.isLocationPermissionGranted()
        _uiState.update { it.copy(locationPermissionGranted = hasPermission) }
        if (hasPermission) {
            loadCurrentLocationWeather()
        } else {
            _uiState.update { it.copy(isLoading = false, showCityPicker = true) }
        }
    }

    fun onPermissionGranted() {
        _uiState.update { it.copy(locationPermissionGranted = true, showCityPicker = false) }
        loadCurrentLocationWeather()
    }

    fun onPermissionDenied() {
        _uiState.update { it.copy(isLoading = false, showCityPicker = true) }
    }

    fun onCitySelected(city: City) {
        _uiState.update {
            it.copy(
                showCityPicker = false,
                currentCity = city,
                currentLocation = city.latLng,
                isLoading = true
            )
        }
        loadWeather(city.latLng.lat, city.latLng.lng, city.name)
    }

    fun onShowCityPicker() {
        _uiState.update { it.copy(showCityPicker = true) }
    }

    fun refresh() {
        val state = _uiState.value
        val loc = state.currentLocation
        val cityName = state.currentCity?.name ?: "Your Location"
        if (loc != null) {
            _uiState.update { it.copy(isLoading = true, weatherError = null) }
            loadWeather(loc.lat, loc.lng, cityName)
        }
    }

    private fun loadCurrentLocationWeather() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val locationResult = locationRepo.getCurrentLocation()) {
                is Result.Success -> {
                    val latLng = locationResult.data
                    _uiState.update { it.copy(currentLocation = latLng) }
                    // Reverse geocode to get city name
                    val cityName = when (val geocodeResult = geocodingRepo.reverseGeocode(latLng.lat, latLng.lng)) {
                        is Result.Success -> geocodeResult.data?.city ?: "Your Location"
                        else -> "Your Location"
                    }
                    loadWeather(latLng.lat, latLng.lng, cityName)
                }
                is Result.Error -> {
                    // Fall back to city picker
                    _uiState.update { it.copy(isLoading = false, showCityPicker = true) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun loadWeather(lat: Double, lon: Double, cityName: String) {
        viewModelScope.launch {
            when (val result = weatherRepo.getWeather(lat, lon, cityName)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            weather = result.data,
                            isOffline = result.data.isFromCache,
                            weatherError = null
                        )
                    }
                }
                is Result.Error -> {
                    // Try cache
                    val key = com.citygo.app.data.repository.WeatherRepositoryImpl.locationKey(lat, lon)
                    weatherRepo.getCachedWeather(key).collect { cached ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                weather = cached,
                                isOffline = cached != null,
                                weatherError = if (cached == null) result.message else null
                            )
                        }
                        return@collect
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }
}
