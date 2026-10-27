package com.citygo.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.citygo.app.data.local.dao.*
import com.citygo.app.data.local.entity.*

@Database(
    entities = [
        WeatherCacheEntity::class,
        MetroStationEntity::class,
        MetroRouteCacheEntity::class,
        BusRouteCacheEntity::class,
        SearchHistoryEntity::class,
        TripPlanEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CityGoDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
    abstract fun metroStationDao(): MetroStationDao
    abstract fun metroRouteCacheDao(): MetroRouteCacheDao
    abstract fun busRouteCacheDao(): BusRouteCacheDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun tripPlanDao(): TripPlanDao
}
