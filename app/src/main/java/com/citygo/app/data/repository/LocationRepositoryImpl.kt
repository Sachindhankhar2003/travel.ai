package com.citygo.app.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.citygo.app.domain.model.LatLng
import com.citygo.app.domain.model.Result
import com.citygo.app.domain.model.safeApiCall
import com.citygo.app.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationRepository {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    override fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Suppress("MissingPermission") // Permission is checked at call site
    override suspend fun getCurrentLocation(): Result<LatLng> {
        if (!isLocationPermissionGranted()) {
            return Result.Error("Location permission not granted")
        }
        return try {
            val location = suspendCancellableCoroutine<Location?> { cont ->
                val cts = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY, cts.token
                ).addOnSuccessListener { loc ->
                    cont.resume(loc)
                }.addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
                cont.invokeOnCancellation { cts.cancel() }
            }
            if (location != null) {
                Result.Success(LatLng(location.latitude, location.longitude))
            } else {
                // Fall back to last known
                getLastKnownLocation()
            }
        } catch (e: Exception) {
            Result.Error("Unable to get location: ${e.message}", e)
        }
    }

    @Suppress("MissingPermission")
    private suspend fun getLastKnownLocation(): Result<LatLng> {
        return suspendCancellableCoroutine { cont ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        cont.resume(Result.Success(LatLng(loc.latitude, loc.longitude)))
                    } else {
                        // Default to Delhi center
                        cont.resume(Result.Success(LatLng(28.6139, 77.2090)))
                    }
                }
                .addOnFailureListener {
                    cont.resume(Result.Success(LatLng(28.6139, 77.2090))) // Delhi fallback
                }
        }
    }
}
