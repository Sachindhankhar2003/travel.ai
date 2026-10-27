package com.citygo.app.data.remote.api

import com.citygo.app.data.remote.dto.HotelPricingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PricingApi {
    /** GET /pricing/hotels?city=Delhi&type=BUDGET
     *  Returns nightly rate in INR for the given city and room type */
    @GET("pricing/hotels")
    suspend fun getHotelPricing(
        @Query("city") city: String,
        @Query("type") type: String   // BUDGET | MID | PREMIUM
    ): HotelPricingResponse
}
