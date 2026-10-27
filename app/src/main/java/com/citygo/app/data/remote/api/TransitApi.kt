package com.citygo.app.data.remote.api

import com.citygo.app.data.remote.dto.BusDto
import com.citygo.app.data.remote.dto.BusSearchResponse
import com.citygo.app.data.remote.dto.MetroRouteDto
import retrofit2.http.*

interface TransitApi {

    // ─── Bus ─────────────────────────────────────────────────────────────────
    @GET("buses/search")
    suspend fun searchBuses(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("type") type: String = "all" // all | govt | private
    ): BusSearchResponse

    @GET("buses/{id}")
    suspend fun getBusById(
        @Path("id") id: String
    ): BusDto

    // ─── Metro ────────────────────────────────────────────────────────────────
    @GET("metro/route")
    suspend fun getMetroRoute(
        @Query("from") from: String,
        @Query("to") to: String
    ): MetroRouteDto

    // ─── Admin ────────────────────────────────────────────────────────────────
    @POST("admin/buses")
    suspend fun createBus(
        @Header("X-Admin-Key") apiKey: String,
        @Body bus: BusDto
    ): BusDto

    @PUT("admin/buses/{id}")
    suspend fun updateBus(
        @Header("X-Admin-Key") apiKey: String,
        @Path("id") id: String,
        @Body bus: BusDto
    ): BusDto

    @DELETE("admin/buses/{id}")
    suspend fun deleteBus(
        @Header("X-Admin-Key") apiKey: String,
        @Path("id") id: String
    ): Map<String, String>
}
