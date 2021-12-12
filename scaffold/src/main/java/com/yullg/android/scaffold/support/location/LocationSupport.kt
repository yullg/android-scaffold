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

/**
 * 提供设备位置相关功能的支持
 */
object LocationSupport {

    /**
     * 获取[LocationManager]实例
     */
    val locationManager: LocationManager? by lazy {
        ContextCompat.getSystemService(Scaffold.context, LocationManager::class.java)
    }

    /**
     * 返回位置的当前启用/禁用状态
     *
     * @see LocationManagerCompat.isLocationEnabled
     */
    fun isLocationEnabled(): Boolean = locationManager?.run {
        LocationManagerCompat.isLocationEnabled(this)
    } ?: false

    /**
     * 从给定的提供程序返回当前位置
     *
     * @see LocationManagerCompat.getCurrentLocation
     */
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