package com.citygo.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ─── Open-Meteo ─────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "current") val current: CurrentWeather,
    @Json(name = "hourly") val hourly: HourlyWeather,
    @Json(name = "daily") val daily: DailyWeather,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    @Json(name = "temperature_2m") val temperature: Double,
    @Json(name = "apparent_temperature") val feelsLike: Double,
    @Json(name = "relative_humidity_2m") val humidity: Int,
    @Json(name = "wind_speed_10m") val windSpeed: Double,
    @Json(name = "weather_code") val weatherCode: Int,
    @Json(name = "precipitation_probability") val precipitationProbability: Int? = null,
    @Json(name = "time") val time: String
)

@JsonClass(generateAdapter = true)
data class HourlyWeather(
    @Json(name = "time") val time: List<String>,
    @Json(name = "temperature_2m") val temperature: List<Double>,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>
)

@JsonClass(generateAdapter = true)
data class DailyWeather(
    @Json(name = "time") val time: List<String>,
    @Json(name = "temperature_2m_max") val tempMax: List<Double>,
    @Json(name = "temperature_2m_min") val tempMin: List<Double>,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "precipitation_probability_max") val precipProbability: List<Int>
)

// ─── Photon Geocoding ────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class PhotonResponse(
    @Json(name = "features") val features: List<PhotonFeature>
)

@JsonClass(generateAdapter = true)
data class PhotonFeature(
    @Json(name = "geometry") val geometry: PhotonGeometry,
    @Json(name = "properties") val properties: PhotonProperties
)

@JsonClass(generateAdapter = true)
data class PhotonGeometry(
    @Json(name = "coordinates") val coordinates: List<Double>
)

@JsonClass(generateAdapter = true)
data class PhotonProperties(
    @Json(name = "name") val name: String? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "state") val state: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "street") val street: String? = null,
    @Json(name = "housenumber") val houseNumber: String? = null
)

// ─── OSRM Routing ────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class OsrmResponse(
    @Json(name = "code") val code: String,
    @Json(name = "routes") val routes: List<OsrmRoute>? = null
)

@JsonClass(generateAdapter = true)
data class OsrmRoute(
    @Json(name = "distance") val distance: Double,
    @Json(name = "duration") val duration: Double,
    @Json(name = "geometry") val geometry: OsrmGeometry,
    @Json(name = "legs") val legs: List<OsrmLeg>
)

@JsonClass(generateAdapter = true)
data class OsrmGeometry(
    @Json(name = "coordinates") val coordinates: List<List<Double>>,
    @Json(name = "type") val type: String
)

@JsonClass(generateAdapter = true)
data class OsrmLeg(
    @Json(name = "distance") val distance: Double,
    @Json(name = "duration") val duration: Double,
    @Json(name = "steps") val steps: List<OsrmStep>? = null
)

@JsonClass(generateAdapter = true)
data class OsrmStep(
    @Json(name = "distance") val distance: Double,
    @Json(name = "duration") val duration: Double,
    @Json(name = "name") val name: String,
    @Json(name = "maneuver") val maneuver: OsrmManeuver
)

@JsonClass(generateAdapter = true)
data class OsrmManeuver(
    @Json(name = "type") val type: String,
    @Json(name = "modifier") val modifier: String? = null,
    @Json(name = "location") val location: List<Double>
)

// ─── Backend Bus and Metro ────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class BusDto(
    @Json(name = "_id") val id: String,
    @Json(name = "route_number") val routeNumber: String,
    @Json(name = "from") val from: String,
    @Json(name = "to") val to: String,
    @Json(name = "stops") val stops: List<String>,
    @Json(name = "operator_type") val operatorType: String, // "Government" | "Private"
    @Json(name = "operator_name") val operatorName: String,
    @Json(name = "fare") val fare: Double,
    @Json(name = "frequency_minutes") val frequencyMinutes: Int,
    @Json(name = "first_bus") val firstBus: String,
    @Json(name = "last_bus") val lastBus: String,
    @Json(name = "ac") val isAC: Boolean = false
)

@JsonClass(generateAdapter = true)
data class BusSearchResponse(
    @Json(name = "buses") val buses: List<BusDto>,
    @Json(name = "total") val total: Int
)

@JsonClass(generateAdapter = true)
data class MetroRouteDto(
    @Json(name = "from_station") val fromStation: String,
    @Json(name = "to_station") val toStation: String,
    @Json(name = "line") val line: String,
    @Json(name = "stations") val stations: List<String>,
    @Json(name = "station_count") val stationCount: Int,
    @Json(name = "fare") val fare: Double,
    @Json(name = "estimated_time_minutes") val estimatedTimeMinutes: Int,
    @Json(name = "first_train") val firstTrain: String,
    @Json(name = "last_train") val lastTrain: String,
    @Json(name = "interchange_stations") val interchangeStations: List<String> = emptyList()
)

// ─── Pricing ──────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class HotelPricingResponse(
    @Json(name = "city") val city: String,
    @Json(name = "type") val type: String,
    @Json(name = "price_per_night") val pricePerNight: Double,
    @Json(name = "currency") val currency: String = "INR"
)
