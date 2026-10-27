package com.citygo.app.data.repository

import com.citygo.app.data.local.dao.WeatherDao
import com.citygo.app.data.local.entity.WeatherCacheEntity
import com.citygo.app.data.remote.api.WeatherApi
import com.citygo.app.domain.model.*
import com.citygo.app.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val dao: WeatherDao
) : WeatherRepository {

    override suspend fun getWeather(lat: Double, lon: Double, cityName: String): Result<Weather> {
        return safeApiCall {
            val response = api.getForecast(lat, lon)
            val current = response.current
            val hourly = response.hourly
            val daily = response.daily

            val hourlyForecasts = hourly.time.indices.take(24).map { i ->
                HourlyForecast(
                    time = hourly.time[i],
                    temperature = hourly.temperature[i],
                    weatherCode = hourly.weatherCode[i],
                    precipProbability = hourly.precipitationProbability[i]
                )
            }

            val dailyForecasts = daily.time.indices.map { i ->
                DailyForecast(
                    date = daily.time[i],
                    tempMax = daily.tempMax[i],
                    tempMin = daily.tempMin[i],
                    weatherCode = daily.weatherCode[i],
                    precipProbability = daily.precipProbability[i]
                )
            }

            val weather = Weather(
                temperature = current.temperature,
                feelsLike = current.feelsLike,
                humidity = current.humidity,
                windSpeed = current.windSpeed,
                weatherCode = current.weatherCode,
                precipProbability = current.precipitationProbability ?: 0,
                cityName = cityName,
                hourlyForecast = hourlyForecasts,
                dailyForecast = dailyForecasts
            )

            // Cache it
            val key = locationKey(lat, lon)
            dao.insertWeatherCache(
                WeatherCacheEntity(
                    locationKey = key,
                    temperature = weather.temperature,
                    feelsLike = weather.feelsLike,
                    humidity = weather.humidity,
                    windSpeed = weather.windSpeed,
                    weatherCode = weather.weatherCode,
                    precipProbability = weather.precipProbability,
                    hourlyJson = Json.encodeToString(hourlyForecasts.map { listOf(it.time, it.temperature.toString(), it.weatherCode.toString(), it.precipProbability.toString()) }),
                    dailyJson = Json.encodeToString(dailyForecasts.map { listOf(it.date, it.tempMax.toString(), it.tempMin.toString(), it.weatherCode.toString(), it.precipProbability.toString()) }),
                    cityName = cityName
                )
            )

            weather
        }
    }

    override fun getCachedWeather(locationKey: String): Flow<Weather?> = flow {
        val cached = dao.getWeatherCache(locationKey)
        if (cached != null) {
            emit(Weather(
                temperature = cached.temperature,
                feelsLike = cached.feelsLike,
                humidity = cached.humidity,
                windSpeed = cached.windSpeed,
                weatherCode = cached.weatherCode,
                precipProbability = cached.precipProbability,
                cityName = cached.cityName,
                hourlyForecast = emptyList(),
                dailyForecast = emptyList(),
                isFromCache = true
            ))
        } else {
            emit(null)
        }
    }

    companion object {
        fun locationKey(lat: Double, lon: Double): String {
            val roundedLat = lat.toBigDecimal().setScale(2, RoundingMode.HALF_UP)
            val roundedLon = lon.toBigDecimal().setScale(2, RoundingMode.HALF_UP)
            return "${roundedLat}_${roundedLon}"
        }
    }
}
