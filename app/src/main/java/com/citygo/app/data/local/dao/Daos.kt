package com.citygo.app.data.local.dao

import androidx.room.*
import com.citygo.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE locationKey = :key LIMIT 1")
    suspend fun getWeatherCache(key: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherCache(entity: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE cachedAt < :threshold")
    suspend fun deleteOldCache(threshold: Long)
}

@Dao
interface MetroStationDao {
    @Query("SELECT * FROM metro_stations ORDER BY line, sequenceOnLine")
    fun getAllStations(): Flow<List<MetroStationEntity>>

    @Query("SELECT * FROM metro_stations WHERE line = :line ORDER BY sequenceOnLine")
    fun getStationsByLine(line: String): Flow<List<MetroStationEntity>>

    @Query("SELECT * FROM metro_stations WHERE name LIKE '%' || :query || '%' LIMIT 10")
    suspend fun searchStations(query: String): List<MetroStationEntity>

    @Query("SELECT COUNT(*) FROM metro_stations")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stations: List<MetroStationEntity>)

    @Query("SELECT * FROM metro_stations ORDER BY ((:lat - latitude) * (:lat - latitude) + (:lon - longitude) * (:lon - longitude)) LIMIT 3")
    suspend fun getNearestStations(lat: Double, lon: Double): List<MetroStationEntity>
}

@Dao
interface MetroRouteCacheDao {
    @Query("SELECT * FROM metro_route_cache WHERE routeKey = :key LIMIT 1")
    suspend fun getRouteCache(key: String): MetroRouteCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteCache(entity: MetroRouteCacheEntity)

    @Query("DELETE FROM metro_route_cache WHERE cachedAt < :threshold")
    suspend fun deleteOldCache(threshold: Long)
}

@Dao
interface BusRouteCacheDao {
    @Query("SELECT * FROM bus_route_cache WHERE routeKey = :key LIMIT 1")
    suspend fun getBusCache(key: String): BusRouteCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusCache(entity: BusRouteCacheEntity)

    @Query("DELETE FROM bus_route_cache WHERE cachedAt < :threshold")
    suspend fun deleteOldCache(threshold: Long)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id NOT IN (SELECT id FROM search_history ORDER BY searchedAt DESC LIMIT 20)")
    suspend fun pruneOldSearches()

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}
