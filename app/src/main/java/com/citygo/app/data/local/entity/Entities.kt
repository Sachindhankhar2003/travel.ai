package com.citygo.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

// ─── Weather Cache ────────────────────────────────────────────────────────────
@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val locationKey: String,        // "lat_lon" rounded to 2dp
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
    val precipProbability: Int,
    val hourlyJson: String,                     // serialized JSON
    val dailyJson: String,
    val cityName: String,
    val cachedAt: Long = System.currentTimeMillis()
)

// ─── Metro Stations ───────────────────────────────────────────────────────────
@Entity(tableName = "metro_stations")
data class MetroStationEntity(
    @PrimaryKey val stationId: String,
    val name: String,
    val line: String,
    val lineColor: String,
    val latitude: Double,
    val longitude: Double,
    val sequenceOnLine: Int,
    val isInterchange: Boolean = false,
    val interchange: String? = null,
    val firstTrain: String = "05:30",
    val lastTrain: String = "23:00"
)

// ─── Metro Routes Cache ───────────────────────────────────────────────────────
@Entity(tableName = "metro_route_cache")
data class MetroRouteCacheEntity(
    @PrimaryKey val routeKey: String,           // "from_to"
    val fromStation: String,
    val toStation: String,
    val line: String,
    val stationsJson: String,
    val stationCount: Int,
    val fare: Double,
    val estimatedTimeMinutes: Int,
    val firstTrain: String,
    val lastTrain: String,
    val interchangeJson: String,
    val cachedAt: Long = System.currentTimeMillis()
)

// ─── Bus Route Cache ──────────────────────────────────────────────────────────
@Entity(tableName = "bus_route_cache")
data class BusRouteCacheEntity(
    @PrimaryKey val routeKey: String,           // "from_to_type"
    val from: String,
    val to: String,
    val filterType: String,
    val busesJson: String,
    val cachedAt: Long = System.currentTimeMillis()
)

// ─── Search History ───────────────────────────────────────────────────────────
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val latitude: Double,
    val longitude: Double,
    val displayName: String,
    val searchedAt: Long = System.currentTimeMillis()
)
