package com.citygo.app.data.repository

import com.citygo.app.data.remote.api.RoutingApi
import com.citygo.app.domain.model.*
import com.citygo.app.domain.repository.RoutingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutingRepositoryImpl @Inject constructor(
    private val api: RoutingApi
) : RoutingRepository {

    override suspend fun getRoute(origin: LatLng, destination: LatLng): Result<RouteResult> {
        return safeApiCall {
            val coords = "${origin.lng},${origin.lat};${destination.lng},${destination.lat}"
            val response = api.getRoute(coords)

            val route = response.routes?.firstOrNull()
                ?: throw Exception("No route found")

            val coordinates = route.geometry.coordinates.map { coord ->
                LatLng(coord[1], coord[0]) // OSRM returns [lon, lat]
            }

            val steps = route.legs.flatMap { leg ->
                leg.steps?.map { step ->
                    RouteStep(
                        instruction = formatInstruction(step.maneuver.type, step.maneuver.modifier, step.name),
                        distance = step.distance,
                        duration = step.duration,
                        maneuverType = step.maneuver.type
                    )
                } ?: emptyList()
            }

            RouteResult(
                distance = route.distance,
                duration = route.duration,
                coordinates = coordinates,
                steps = steps
            )
        }
    }

    private fun formatInstruction(type: String, modifier: String?, name: String): String {
        val directionMap = mapOf(
            "turn" to when (modifier) {
                "left" -> "Turn left"
                "right" -> "Turn right"
                "slight left" -> "Slight left"
                "slight right" -> "Slight right"
                "sharp left" -> "Sharp left turn"
                "sharp right" -> "Sharp right turn"
                "uturn" -> "Make a U-turn"
                else -> "Continue"
            },
            "depart" to "Head",
            "arrive" to "You have arrived",
            "merge" to "Merge",
            "ramp" to "Take the ramp",
            "roundabout" to "Enter the roundabout",
            "rotary" to "Enter the rotary"
        )
        val base = directionMap[type] ?: "Continue"
        return if (name.isNotBlank() && name != "(null)") "$base onto $name" else base
    }
}
