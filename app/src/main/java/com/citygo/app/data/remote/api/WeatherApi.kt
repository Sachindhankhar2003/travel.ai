package com.citygo.app.data.remote.api

import com.citygo.app.data.remote.dto.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,apparent_temperature,relative_humidity_2m,wind_speed_10m,weather_code,precipitation_probability",
        @Query("hourly") hourly: String = "temperature_2m,weather_code,precipitation_probability",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weather_code,precipitation_probability_max",
        @Query("timezone") timezone: String = "Asia/Kolkata",
        @Query("forecast_days") forecastDays: Int = 7,
        @Query("wind_speed_unit") windUnit: String = "kmh"
    ): WeatherResponse
}
