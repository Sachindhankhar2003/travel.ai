package com.citygo.app.domain.repository

import com.citygo.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double, cityName: String): Result<Weather>
    fun getCachedWeather(locationKey: String): Flow<Weather?>
}

interface GeocodingRepository {
    suspend fun searchPlaces(query: String): Result<List<Place>>
    suspend fun reverseGeocode(lat: Double, lon: Double): Result<Place?>
    fun getRecentSearches(): Flow<List<Place>>
    suspend fun saveSearch(place: Place)
}

interface RoutingRepository {
    suspend fun getRoute(origin: LatLng, destination: LatLng): Result<RouteResult>
}

interface MetroRepository {
    suspend fun getAllStations(): Result<List<MetroStation>>
    suspend fun searchStations(query: String): Result<List<MetroStation>>
    suspend fun getNearestStations(lat: Double, lon: Double): Result<List<MetroStation>>
    suspend fun getRoute(from: String, to: String): Result<MetroRoute>
    fun getStationsByLine(line: String): Flow<List<MetroStation>>
}

interface BusRepository {
    suspend fun searchBuses(from: String, to: String, type: String): Result<List<Bus>>
    suspend fun getBusById(id: String): Result<Bus>
}

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<LatLng>
    fun isLocationPermissionGranted(): Boolean
}
