package com.citygo.app.data.repository

import com.citygo.app.data.local.dao.SearchHistoryDao
import com.citygo.app.data.local.entity.SearchHistoryEntity
import com.citygo.app.data.remote.api.GeocodingApi
import com.citygo.app.domain.model.*
import com.citygo.app.domain.repository.GeocodingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingRepositoryImpl @Inject constructor(
    private val api: GeocodingApi,
    private val dao: SearchHistoryDao
) : GeocodingRepository {

    override suspend fun searchPlaces(query: String): Result<List<Place>> {
        return safeApiCall {
            val response = api.search(query)
            response.features.map { feature ->
                val props = feature.properties
                val coords = feature.geometry.coordinates
                Place(
                    name = props.name ?: props.street ?: query,
                    displayName = buildDisplayName(props.name, props.street, props.houseNumber, props.city, props.state),
                    latLng = LatLng(coords[1], coords[0]),
                    city = props.city,
                    state = props.state
                )
            }
        }
    }

    override suspend fun reverseGeocode(lat: Double, lon: Double): Result<Place?> {
        return safeApiCall {
            val response = api.reverse(lat, lon)
            response.features.firstOrNull()?.let { feature ->
                val props = feature.properties
                val coords = feature.geometry.coordinates
                Place(
                    name = props.name ?: props.city ?: "Unknown",
                    displayName = buildDisplayName(props.name, props.street, props.houseNumber, props.city, props.state),
                    latLng = LatLng(coords[1], coords[0]),
                    city = props.city,
                    state = props.state
                )
            }
        }
    }

    override fun getRecentSearches(): Flow<List<Place>> {
        return dao.getRecentSearches().map { entities ->
            entities.map { entity ->
                Place(
                    name = entity.query,
                    displayName = entity.displayName,
                    latLng = LatLng(entity.latitude, entity.longitude)
                )
            }
        }
    }

    override suspend fun saveSearch(place: Place) {
        dao.insertSearch(
            SearchHistoryEntity(
                query = place.name,
                latitude = place.latLng.lat,
                longitude = place.latLng.lng,
                displayName = place.displayName
            )
        )
        dao.pruneOldSearches()
    }

    private fun buildDisplayName(
        name: String?,
        street: String?,
        houseNumber: String?,
        city: String?,
        state: String?
    ): String {
        val parts = mutableListOf<String>()
        if (!name.isNullOrBlank()) parts.add(name)
        else if (!houseNumber.isNullOrBlank() && !street.isNullOrBlank()) parts.add("$houseNumber $street")
        else if (!street.isNullOrBlank()) parts.add(street)
        if (!city.isNullOrBlank()) parts.add(city)
        if (!state.isNullOrBlank()) parts.add(state)
        return parts.joinToString(", ")
    }
}
