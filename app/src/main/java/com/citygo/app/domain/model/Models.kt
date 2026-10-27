package com.citygo.app.domain.model

// ─── Location ─────────────────────────────────────────────────────────────────
data class LatLng(val lat: Double, val lng: Double)

data class Place(
    val name: String,
    val displayName: String,
    val latLng: LatLng,
    val city: String? = null,
    val state: String? = null
)

// ─── Weather ──────────────────────────────────────────────────────────────────
data class Weather(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
    val precipProbability: Int,
    val cityName: String,
    val hourlyForecast: List<HourlyForecast>,
    val dailyForecast: List<DailyForecast>,
    val isFromCache: Boolean = false
)

data class HourlyForecast(
    val time: String,
    val temperature: Double,
    val weatherCode: Int,
    val precipProbability: Int
)

data class DailyForecast(
    val date: String,
    val tempMax: Double,
    val tempMin: Double,
    val weatherCode: Int,
    val precipProbability: Int
)

fun Int.toWeatherDescription(): String = when (this) {
    0 -> "Clear Sky"
    1, 2, 3 -> "Partly Cloudy"
    45, 48 -> "Foggy"
    51, 53, 55 -> "Drizzle"
    61, 63, 65 -> "Rain"
    71, 73, 75 -> "Snow"
    80, 81, 82 -> "Showers"
    95 -> "Thunderstorm"
    96, 99 -> "Hail"
    else -> "Unknown"
}

fun Int.toWeatherIcon(): String = when (this) {
    0 -> "wb_sunny"
    1, 2, 3 -> "partly_cloudy_day"
    45, 48 -> "foggy"
    51, 53, 55, 61, 63, 65 -> "water_drop"
    71, 73, 75 -> "ac_unit"
    80, 81, 82 -> "rainy"
    95, 96, 99 -> "thunderstorm"
    else -> "cloud"
}

// ─── Metro ────────────────────────────────────────────────────────────────────
data class MetroStation(
    val stationId: String,
    val name: String,
    val line: String,
    val lineColor: String,
    val latLng: LatLng,
    val sequenceOnLine: Int,
    val isInterchange: Boolean,
    val interchange: String?,
    val firstTrain: String,
    val lastTrain: String
)

data class MetroRoute(
    val fromStation: String,
    val toStation: String,
    val line: String,
    val stations: List<String>,
    val stationCount: Int,
    val fare: Double,
    val estimatedTimeMinutes: Int,
    val firstTrain: String,
    val lastTrain: String,
    val interchangeStations: List<String>,
    val isFromCache: Boolean = false
)

// ─── Bus ──────────────────────────────────────────────────────────────────────
enum class OperatorType { GOVERNMENT, PRIVATE, ALL }

data class Bus(
    val id: String,
    val routeNumber: String,
    val from: String,
    val to: String,
    val stops: List<String>,
    val operatorType: OperatorType,
    val operatorName: String,
    val fare: Double,
    val frequencyMinutes: Int,
    val firstBus: String,
    val lastBus: String,
    val isAC: Boolean
)

// ─── Route / Navigation ───────────────────────────────────────────────────────
data class RouteResult(
    val distance: Double,       // meters
    val duration: Double,       // seconds
    val coordinates: List<LatLng>,
    val steps: List<RouteStep>
)

data class RouteStep(
    val instruction: String,
    val distance: Double,
    val duration: Double,
    val maneuverType: String
)

// ─── City ─────────────────────────────────────────────────────────────────────
data class City(
    val name: String,
    val state: String,
    val latLng: LatLng,
    val isPopular: Boolean = false
)
