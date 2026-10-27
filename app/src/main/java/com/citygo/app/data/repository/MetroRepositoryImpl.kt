package com.citygo.app.data.repository

import com.citygo.app.data.local.dao.MetroRouteCacheDao
import com.citygo.app.data.local.dao.MetroStationDao
import com.citygo.app.data.local.entity.MetroRouteCacheEntity
import com.citygo.app.data.local.entity.MetroStationEntity
import com.citygo.app.data.remote.api.TransitApi
import com.citygo.app.domain.model.*
import com.citygo.app.domain.repository.MetroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetroRepositoryImpl @Inject constructor(
    private val api: TransitApi,
    private val stationDao: MetroStationDao,
    private val routeCacheDao: MetroRouteCacheDao
) : MetroRepository {

    override suspend fun getAllStations(): Result<List<MetroStation>> = safeApiCall {
        // Try local first
        val count = stationDao.getCount()
        if (count == 0) {
            seedMetroStations()
        }
        stationDao.searchStations("").map { it.toDomain() }
    }

    override suspend fun searchStations(query: String): Result<List<MetroStation>> = safeApiCall {
        if (stationDao.getCount() == 0) seedMetroStations()
        stationDao.searchStations(query).map { it.toDomain() }
    }

    override suspend fun getNearestStations(lat: Double, lon: Double): Result<List<MetroStation>> = safeApiCall {
        if (stationDao.getCount() == 0) seedMetroStations()
        stationDao.getNearestStations(lat, lon).map { it.toDomain() }
    }

    override suspend fun getRoute(from: String, to: String): Result<MetroRoute> {
        val key = "${from}_${to}"
        val cached = routeCacheDao.getRouteCache(key)
        if (cached != null && System.currentTimeMillis() - cached.cachedAt < CACHE_TTL_MS) {
            return Result.Success(cached.toDomain(isFromCache = true))
        }
        return safeApiCall {
            val dto = api.getMetroRoute(from, to)
            val route = MetroRoute(
                fromStation = dto.fromStation,
                toStation = dto.toStation,
                line = dto.line,
                stations = dto.stations,
                stationCount = dto.stationCount,
                fare = dto.fare,
                estimatedTimeMinutes = dto.estimatedTimeMinutes,
                firstTrain = dto.firstTrain,
                lastTrain = dto.lastTrain,
                interchangeStations = dto.interchangeStations
            )
            routeCacheDao.insertRouteCache(route.toEntity(key))
            route
        }
    }

    override fun getStationsByLine(line: String): Flow<List<MetroStation>> =
        stationDao.getStationsByLine(line).map { it.map { e -> e.toDomain() } }

    // ── Seed static Delhi Metro data ──────────────────────────────────────────
    private suspend fun seedMetroStations() {
        val stations = buildDelhiMetroStations()
        stationDao.insertAll(stations)
    }

    private fun buildDelhiMetroStations(): List<MetroStationEntity> = listOf(
        // Yellow Line (Samaypur Badli ↔ Huda City Centre)
        MetroStationEntity("YL_01","Samaypur Badli","Yellow Line","#FBBF24",28.7288,77.1327,1),
        MetroStationEntity("YL_02","Rohini Sector 18,19","Yellow Line","#FBBF24",28.7178,77.1305,2),
        MetroStationEntity("YL_03","Haiderpur Badli Mor","Yellow Line","#FBBF24",28.7129,77.1433,3),
        MetroStationEntity("YL_04","Jahangirpuri","Yellow Line","#FBBF24",28.7271,77.1614,4),
        MetroStationEntity("YL_05","Adarsh Nagar","Yellow Line","#FBBF24",28.7162,77.1697,5),
        MetroStationEntity("YL_06","Azadpur","Yellow Line","#FBBF24",28.7050,77.1773,6,true,"Red Line"),
        MetroStationEntity("YL_07","Model Town","Yellow Line","#FBBF24",28.7027,77.1902,7),
        MetroStationEntity("YL_08","G.T.B. Nagar","Yellow Line","#FBBF24",28.6930,77.2027,8),
        MetroStationEntity("YL_09","Vishwa Vidyalaya","Yellow Line","#FBBF24",28.6890,77.2135,9),
        MetroStationEntity("YL_10","Vidhan Sabha","Yellow Line","#FBBF24",28.6781,77.2178,10),
        MetroStationEntity("YL_11","Civil Lines","Yellow Line","#FBBF24",28.6716,77.2250,11),
        MetroStationEntity("YL_12","Kashmere Gate","Yellow Line","#FBBF24",28.6680,77.2292,12,true,"Red Line,Violet Line"),
        MetroStationEntity("YL_13","Chandni Chowk","Yellow Line","#FBBF24",28.6582,77.2304,13),
        MetroStationEntity("YL_14","Chawri Bazar","Yellow Line","#FBBF24",28.6489,77.2285,14),
        MetroStationEntity("YL_15","New Delhi","Yellow Line","#FBBF24",28.6428,77.2218,15,true,"Airport Express"),
        MetroStationEntity("YL_16","Rajiv Chowk","Yellow Line","#FBBF24",28.6330,77.2195,16,true,"Blue Line"),
        MetroStationEntity("YL_17","Patel Chowk","Yellow Line","#FBBF24",28.6210,77.2102,17),
        MetroStationEntity("YL_18","Central Secretariat","Yellow Line","#FBBF24",28.6135,77.2118,18,true,"Violet Line"),
        MetroStationEntity("YL_19","Udyog Bhawan","Yellow Line","#FBBF24",28.6071,77.2178,19),
        MetroStationEntity("YL_20","Lok Kalyan Marg","Yellow Line","#FBBF24",28.5997,77.2174,20),
        MetroStationEntity("YL_21","Jor Bagh","Yellow Line","#FBBF24",28.5894,77.2167,21),
        MetroStationEntity("YL_22","INA","Yellow Line","#FBBF24",28.5761,77.2145,22,true,"Pink Line"),
        MetroStationEntity("YL_23","AIIMS","Yellow Line","#FBBF24",28.5684,77.2102,23),
        MetroStationEntity("YL_24","Green Park","Yellow Line","#FBBF24",28.5612,77.2049,24),
        MetroStationEntity("YL_25","Hauz Khas","Yellow Line","#FBBF24",28.5440,77.2063,25,true,"Magenta Line"),
        MetroStationEntity("YL_26","Malviya Nagar","Yellow Line","#FBBF24",28.5307,77.2112,26),
        MetroStationEntity("YL_27","Saket","Yellow Line","#FBBF24",28.5233,77.2115,27),
        MetroStationEntity("YL_28","Qutab Minar","Yellow Line","#FBBF24",28.5176,77.1958,28),
        MetroStationEntity("YL_29","Chhattarpur","Yellow Line","#FBBF24",28.5065,77.1858,29),
        MetroStationEntity("YL_30","Sultanpur","Yellow Line","#FBBF24",28.4972,77.1769,30),
        MetroStationEntity("YL_31","Ghitorni","Yellow Line","#FBBF24",28.4843,77.1618,31),
        MetroStationEntity("YL_32","Arjan Garh","Yellow Line","#FBBF24",28.4638,77.1297,32),
        MetroStationEntity("YL_33","Guru Dronacharya","Yellow Line","#FBBF24",28.4575,77.1005,33),
        MetroStationEntity("YL_34","Sikanderpur","Yellow Line","#FBBF24",28.4603,77.0790,34,true,"Rapid Metro"),
        MetroStationEntity("YL_35","MG Road","Yellow Line","#FBBF24",28.4624,77.0640,35),
        MetroStationEntity("YL_36","IFFCO Chowk","Yellow Line","#FBBF24",28.4718,77.0714,36),
        MetroStationEntity("YL_37","HUDA City Centre","Yellow Line","#FBBF24",28.4594,77.0732,37),
        // Blue Line (Dwarka Sector 21 ↔ Vaishali/Noida Electronic City)
        MetroStationEntity("BL_01","Dwarka Sector 21","Blue Line","#3B82F6",28.5590,77.0588,1),
        MetroStationEntity("BL_02","Dwarka Sector 8","Blue Line","#3B82F6",28.5645,77.0725,2),
        MetroStationEntity("BL_03","Dwarka Sector 9","Blue Line","#3B82F6",28.5731,77.0786,3),
        MetroStationEntity("BL_04","Dwarka Sector 10","Blue Line","#3B82F6",28.5794,77.0837,4),
        MetroStationEntity("BL_05","Dwarka Sector 11","Blue Line","#3B82F6",28.5856,77.0882,5),
        MetroStationEntity("BL_06","Dwarka Sector 12","Blue Line","#3B82F6",28.5889,77.0943,6),
        MetroStationEntity("BL_07","Dwarka Sector 13","Blue Line","#3B82F6",28.5914,77.1001,7),
        MetroStationEntity("BL_08","Dwarka Sector 14","Blue Line","#3B82F6",28.5944,77.1078,8),
        MetroStationEntity("BL_09","Dwarka","Blue Line","#3B82F6",28.5921,77.1168,9),
        MetroStationEntity("BL_10","Dwarka Mor","Blue Line","#3B82F6",28.6072,77.1188,10),
        MetroStationEntity("BL_11","Nawada","Blue Line","#3B82F6",28.6228,77.1261,11),
        MetroStationEntity("BL_12","Uttam Nagar West","Blue Line","#3B82F6",28.6249,77.1372,12),
        MetroStationEntity("BL_13","Uttam Nagar East","Blue Line","#3B82F6",28.6268,77.1476,13),
        MetroStationEntity("BL_14","Janakpuri West","Blue Line","#3B82F6",28.6276,77.1576,14),
        MetroStationEntity("BL_15","Janakpuri East","Blue Line","#3B82F6",28.6337,77.1724,15),
        MetroStationEntity("BL_16","Tilak Nagar","Blue Line","#3B82F6",28.6390,77.1820,16),
        MetroStationEntity("BL_17","Subhash Nagar","Blue Line","#3B82F6",28.6394,77.1912,17),
        MetroStationEntity("BL_18","Tagore Garden","Blue Line","#3B82F6",28.6417,77.1987,18),
        MetroStationEntity("BL_19","Rajouri Garden","Blue Line","#3B82F6",28.6483,77.2088,19,true,"Pink Line"),
        MetroStationEntity("BL_20","Ramesh Nagar","Blue Line","#3B82F6",28.6529,77.2165,20),
        MetroStationEntity("BL_21","Moti Nagar","Blue Line","#3B82F6",28.6565,77.2254,21),
        MetroStationEntity("BL_22","Kirti Nagar","Blue Line","#3B82F6",28.6561,77.2363,22,true,"Green Line"),
        MetroStationEntity("BL_23","Shadipur","Blue Line","#3B82F6",28.6522,77.2499,23),
        MetroStationEntity("BL_24","Patel Nagar","Blue Line","#3B82F6",28.6549,77.2591,24),
        MetroStationEntity("BL_25","Rajendra Place","Blue Line","#3B82F6",28.6556,77.2680,25),
        MetroStationEntity("BL_26","Karol Bagh","Blue Line","#3B82F6",28.6510,77.2775,26),
        MetroStationEntity("BL_27","Jhandewalan","Blue Line","#3B82F6",28.6499,77.2867,27),
        MetroStationEntity("BL_28","Ramakrishna Ashram Marg","Blue Line","#3B82F6",28.6453,77.2925,28),
        MetroStationEntity("BL_29","Rajiv Chowk","Blue Line","#3B82F6",28.6330,77.2195,29,true,"Yellow Line"),
        MetroStationEntity("BL_30","Mandi House","Blue Line","#3B82F6",28.6265,77.2365,30,true,"Violet Line"),
        MetroStationEntity("BL_31","Supreme Court","Blue Line","#3B82F6",28.6232,77.2451,31),
        MetroStationEntity("BL_32","Pragati Maidan","Blue Line","#3B82F6",28.6186,77.2531,32),
        MetroStationEntity("BL_33","Indraprastha","Blue Line","#3B82F6",28.6115,77.2607,33),
        MetroStationEntity("BL_34","Yamuna Bank","Blue Line","#3B82F6",28.6086,77.2836,34,true,"Blue Line Branch"),
        MetroStationEntity("BL_35","Akshardham","Blue Line","#3B82F6",28.6127,77.3001,35),
        MetroStationEntity("BL_36","Mayur Vihar - 1","Blue Line","#3B82F6",28.6074,77.3111,36),
        MetroStationEntity("BL_37","Mayur Vihar Extension","Blue Line","#3B82F6",28.6029,77.3237,37),
        MetroStationEntity("BL_38","New Ashok Nagar","Blue Line","#3B82F6",28.5999,77.3345,38),
        MetroStationEntity("BL_39","Noida Sector 15","Blue Line","#3B82F6",28.5937,77.3407,39),
        MetroStationEntity("BL_40","Noida Sector 16","Blue Line","#3B82F6",28.5875,77.3423,40),
        MetroStationEntity("BL_41","Noida Sector 18","Blue Line","#3B82F6",28.5734,77.3281,41,true,"Aqua Line"),
        MetroStationEntity("BL_42","Botanical Garden","Blue Line","#3B82F6",28.5619,77.3353,42,true,"Magenta Line"),
        MetroStationEntity("BL_43","Noida Sector 52","Blue Line","#3B82F6",28.5551,77.3589,43),
        MetroStationEntity("BL_44","Noida Sector 61","Blue Line","#3B82F6",28.5530,77.3673,44),
        MetroStationEntity("BL_45","Noida Sector 59","Blue Line","#3B82F6",28.5510,77.3753,45),
        MetroStationEntity("BL_46","Noida Sector 62","Blue Line","#3B82F6",28.5592,77.3828,46),
        MetroStationEntity("BL_47","Noida Electronic City","Blue Line","#3B82F6",28.5743,77.3820,47),
        // Rapid Metro Gurugram
        MetroStationEntity("RM_01","Sikanderpur","Rapid Metro","#8B5CF6",28.4603,77.0790,1),
        MetroStationEntity("RM_02","Sector 42-43","Rapid Metro","#8B5CF6",28.4601,77.0888,2),
        MetroStationEntity("RM_03","Sector 53-54","Rapid Metro","#8B5CF6",28.4563,77.1011,3),
        MetroStationEntity("RM_04","Sector 54 Chowk","Rapid Metro","#8B5CF6",28.4467,77.1028,4),
        MetroStationEntity("RM_05","Sector 55-56","Rapid Metro","#8B5CF6",28.4384,77.1028,5),
        MetroStationEntity("RM_06","Belvedere Towers","Rapid Metro","#8B5CF6",28.4363,77.0988,6),
        MetroStationEntity("RM_07","Phase 3 Rapid Metro","Rapid Metro","#8B5CF6",28.4352,77.0888,7),
        MetroStationEntity("RM_08","Phase 2 Rapid Metro","Rapid Metro","#8B5CF6",28.4344,77.0780,8),
        MetroStationEntity("RM_09","Vodafone Belvedere","Rapid Metro","#8B5CF6",28.4337,77.0695,9),
        MetroStationEntity("RM_10","Moulsari Avenue","Rapid Metro","#8B5CF6",28.4383,77.0628,10),
        // Noida Metro (Aqua Line)
        MetroStationEntity("NM_01","Sector 76","Aqua Line","#06B6D4",28.6028,77.3935,1),
        MetroStationEntity("NM_02","Sector 101","Aqua Line","#06B6D4",28.5880,77.3812,2),
        MetroStationEntity("NM_03","Sector 81","Aqua Line","#06B6D4",28.5750,77.3920,3),
        MetroStationEntity("NM_04","NSEZ","Aqua Line","#06B6D4",28.5664,77.3918,4),
        MetroStationEntity("NM_05","Sector 83","Aqua Line","#06B6D4",28.5588,77.3856,5),
        MetroStationEntity("NM_06","Sector 137","Aqua Line","#06B6D4",28.5478,77.3759,6),
        MetroStationEntity("NM_07","Sector 142","Aqua Line","#06B6D4",28.5401,77.3776,7),
        MetroStationEntity("NM_08","Sector 143","Aqua Line","#06B6D4",28.5353,77.3793,8),
        MetroStationEntity("NM_09","Sector 144","Aqua Line","#06B6D4",28.5282,77.3789,9),
        MetroStationEntity("NM_10","Sector 145","Aqua Line","#06B6D4",28.5213,77.3794,10),
        MetroStationEntity("NM_11","Sector 146","Aqua Line","#06B6D4",28.5124,77.3770,11),
        MetroStationEntity("NM_12","Sector 147","Aqua Line","#06B6D4",28.5049,77.3730,12),
        MetroStationEntity("NM_13","Sector 148","Aqua Line","#06B6D4",28.4964,77.3748,13),
        MetroStationEntity("NM_14","Pari Chowk","Aqua Line","#06B6D4",28.4797,77.3700,14),
        MetroStationEntity("NM_15","Alpha 1","Aqua Line","#06B6D4",28.4745,77.3756,15),
        MetroStationEntity("NM_16","Delta 1","Aqua Line","#06B6D4",28.4693,77.3817,16),
        MetroStationEntity("NM_17","GNIDA Office","Aqua Line","#06B6D4",28.4640,77.3830,17),
        MetroStationEntity("NM_18","Depot Station","Aqua Line","#06B6D4",28.4596,77.3841,18)
    )

    companion object {
        private const val CACHE_TTL_MS = 6 * 60 * 60 * 1000L // 6 hours
    }
}

private fun MetroStationEntity.toDomain() = MetroStation(
    stationId = stationId,
    name = name,
    line = line,
    lineColor = lineColor,
    latLng = LatLng(latitude, longitude),
    sequenceOnLine = sequenceOnLine,
    isInterchange = isInterchange,
    interchange = interchange,
    firstTrain = firstTrain,
    lastTrain = lastTrain
)

private fun MetroRouteCacheEntity.toDomain(isFromCache: Boolean = false) = MetroRoute(
    fromStation = fromStation,
    toStation = toStation,
    line = line,
    stations = runCatching { Json.decodeFromString<List<String>>(stationsJson) }.getOrElse { emptyList() },
    stationCount = stationCount,
    fare = fare,
    estimatedTimeMinutes = estimatedTimeMinutes,
    firstTrain = firstTrain,
    lastTrain = lastTrain,
    interchangeStations = runCatching { Json.decodeFromString<List<String>>(interchangeJson) }.getOrElse { emptyList() },
    isFromCache = isFromCache
)

private fun MetroRoute.toEntity(key: String) = MetroRouteCacheEntity(
    routeKey = key,
    fromStation = fromStation,
    toStation = toStation,
    line = line,
    stationsJson = Json.encodeToString(stations),
    stationCount = stationCount,
    fare = fare,
    estimatedTimeMinutes = estimatedTimeMinutes,
    firstTrain = firstTrain,
    lastTrain = lastTrain,
    interchangeJson = Json.encodeToString(interchangeStations)
)
