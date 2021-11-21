package com.yullg.android.scaffold.support.location

import android.Manifest
import android.location.Location
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import com.yullg.android.scaffold.app.Scaffold
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationSupport {

    val locationManager: LocationManager? by lazy {
        ContextCompat.getSystemService(Scaffold.context, LocationManager::class.java)
    }

    fun isLocationEnabled(): Boolean = locationManager?.run {
        LocationManagerCompat.isLocationEnabled(this)
    } ?: false

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    suspend fun getCurrentLocation(provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            LocationManagerCompat.getCurrentLocation(
                locationManager!!,
                provider,
                cancellationSignal,
                ContextCompat.getMainExecutor(Scaffold.context)
            ) { location -> continuation.resume(location) }
            continuation.invokeOnCancellation {
                cancellationSignal.cancel()
            }
        }

}