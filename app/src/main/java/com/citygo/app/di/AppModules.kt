package com.citygo.app.di

import android.content.Context
import androidx.room.Room
import com.citygo.app.BuildConfig
import com.citygo.app.data.local.CityGoDatabase
import com.citygo.app.data.local.dao.*
import com.citygo.app.data.remote.api.*
import com.citygo.app.data.repository.*
import com.citygo.app.domain.repository.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @Named("weather")
    fun provideWeatherRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.OPEN_METEO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    @Named("geocoding")
    fun provideGeocodingRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.PHOTON_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    @Named("routing")
    fun provideRoutingRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.OSRM_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    @Named("backend")
    fun provideBackendRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideWeatherApi(@Named("weather") retrofit: Retrofit): WeatherApi =
        retrofit.create(WeatherApi::class.java)

    @Provides
    @Singleton
    fun provideGeocodingApi(@Named("geocoding") retrofit: Retrofit): GeocodingApi =
        retrofit.create(GeocodingApi::class.java)

    @Provides
    @Singleton
    fun provideRoutingApi(@Named("routing") retrofit: Retrofit): RoutingApi =
        retrofit.create(RoutingApi::class.java)

    @Provides
    @Singleton
    fun provideTransitApi(@Named("backend") retrofit: Retrofit): TransitApi =
        retrofit.create(TransitApi::class.java)

    @Provides
    @Singleton
    fun providePricingApi(@Named("backend") retrofit: Retrofit): PricingApi =
        retrofit.create(PricingApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CityGoDatabase =
        Room.databaseBuilder(context, CityGoDatabase::class.java, "citygo_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideWeatherDao(db: CityGoDatabase) = db.weatherDao()
    @Provides fun provideMetroStationDao(db: CityGoDatabase) = db.metroStationDao()
    @Provides fun provideMetroRouteCacheDao(db: CityGoDatabase) = db.metroRouteCacheDao()
    @Provides fun provideBusRouteCacheDao(db: CityGoDatabase) = db.busRouteCacheDao()
    @Provides fun provideSearchHistoryDao(db: CityGoDatabase) = db.searchHistoryDao()
    @Provides fun provideTripPlanDao(db: CityGoDatabase) = db.tripPlanDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindWeatherRepo(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds @Singleton
    abstract fun bindGeocodingRepo(impl: GeocodingRepositoryImpl): GeocodingRepository

    @Binds @Singleton
    abstract fun bindRoutingRepo(impl: RoutingRepositoryImpl): RoutingRepository

    @Binds @Singleton
    abstract fun bindMetroRepo(impl: MetroRepositoryImpl): MetroRepository

    @Binds @Singleton
    abstract fun bindBusRepo(impl: BusRepositoryImpl): BusRepository

    @Binds @Singleton
    abstract fun bindLocationRepo(impl: LocationRepositoryImpl): LocationRepository
}
