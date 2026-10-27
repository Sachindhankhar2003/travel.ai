package com.citygo.app.data.remote.api

import com.citygo.app.data.remote.dto.PhotonResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("api")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("lang") lang: String = "en",
        @Query("bbox") bbox: String = "68.11,20.67,97.39,37.09" // India bounding box
    ): PhotonResponse

    @GET("reverse")
    suspend fun reverse(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("lang") lang: String = "en"
    ): PhotonResponse
}
