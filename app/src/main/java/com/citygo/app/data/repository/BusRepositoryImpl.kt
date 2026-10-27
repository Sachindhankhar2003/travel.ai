package com.citygo.app.data.repository

import com.citygo.app.data.local.dao.BusRouteCacheDao
import com.citygo.app.data.local.entity.BusRouteCacheEntity
import com.citygo.app.data.remote.api.TransitApi
import com.citygo.app.domain.model.*
import com.citygo.app.domain.repository.BusRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusRepositoryImpl @Inject constructor(
    private val api: TransitApi,
    private val cacheDao: BusRouteCacheDao
) : BusRepository {

    override suspend fun searchBuses(from: String, to: String, type: String): Result<List<Bus>> {
        val key = "${from}_${to}_$type"
        val cached = cacheDao.getBusCache(key)
        if (cached != null && System.currentTimeMillis() - cached.cachedAt < CACHE_TTL_MS) {
            return safeApiCall {
                // Return from cache
                val buses = Json.decodeFromString<List<BusSerializable>>(cached.busesJson)
                buses.map { it.toDomain() }
            }
        }

        return safeApiCall {
            val response = api.searchBuses(from, to, type)
            val buses = response.buses.map { dto ->
                Bus(
                    id = dto.id,
                    routeNumber = dto.routeNumber,
                    from = dto.from,
                    to = dto.to,
                    stops = dto.stops,
                    operatorType = when (dto.operatorType.lowercase()) {
                        "government" -> OperatorType.GOVERNMENT
                        else -> OperatorType.PRIVATE
                    },
                    operatorName = dto.operatorName,
                    fare = dto.fare,
                    frequencyMinutes = dto.frequencyMinutes,
                    firstBus = dto.firstBus,
                    lastBus = dto.lastBus,
                    isAC = dto.isAC
                )
            }

            // Cache
            val serializable = buses.map { it.toSerializable() }
            cacheDao.insertBusCache(
                BusRouteCacheEntity(
                    routeKey = key,
                    from = from,
                    to = to,
                    filterType = type,
                    busesJson = Json.encodeToString(serializable)
                )
            )

            buses
        }
    }

    override suspend fun getBusById(id: String): Result<Bus> = safeApiCall {
        val dto = api.getBusById(id)
        Bus(
            id = dto.id,
            routeNumber = dto.routeNumber,
            from = dto.from,
            to = dto.to,
            stops = dto.stops,
            operatorType = when (dto.operatorType.lowercase()) {
                "government" -> OperatorType.GOVERNMENT
                else -> OperatorType.PRIVATE
            },
            operatorName = dto.operatorName,
            fare = dto.fare,
            frequencyMinutes = dto.frequencyMinutes,
            firstBus = dto.firstBus,
            lastBus = dto.lastBus,
            isAC = dto.isAC
        )
    }

    companion object {
        private const val CACHE_TTL_MS = 30 * 60 * 1000L // 30 minutes
    }
}

@kotlinx.serialization.Serializable
data class BusSerializable(
    val id: String, val routeNumber: String, val from: String, val to: String,
    val stops: List<String>, val operatorType: String, val operatorName: String,
    val fare: Double, val frequencyMinutes: Int, val firstBus: String,
    val lastBus: String, val isAC: Boolean
) {
    fun toDomain() = Bus(
        id, routeNumber, from, to, stops,
        if (operatorType == "GOVERNMENT") OperatorType.GOVERNMENT else OperatorType.PRIVATE,
        operatorName, fare, frequencyMinutes, firstBus, lastBus, isAC
    )
}

private fun Bus.toSerializable() = BusSerializable(
    id, routeNumber, from, to, stops, operatorType.name,
    operatorName, fare, frequencyMinutes, firstBus, lastBus, isAC
)
