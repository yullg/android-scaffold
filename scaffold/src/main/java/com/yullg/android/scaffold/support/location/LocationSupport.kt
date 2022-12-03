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
 * 提供设备定位相关功能的支持
 */
object LocationSupport {

    /**
     * 获取[LocationManager]实例
     */
    val locationManager: LocationManager? by lazy {
        ContextCompat.getSystemService(Scaffold.context, LocationManager::class.java)
    }

    /**
     * 检查定位服务是否开启
     *
     * @see LocationManagerCompat.isLocationEnabled
     */
    fun isLocationEnabled(): Boolean = locationManager?.run {
        LocationManagerCompat.isLocationEnabled(this)
    } ?: false

    /**
     * 从指定的提供程序获取当前位置
     *
     * @see LocationManagerCompat.getCurrentLocation
     */
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    suspend fun getCurrentLocation(
        provider: String,
        @CacheUseMode cacheUseMode: Int = CacheUseMode.IGNORE
    ): Location? {
        val localLocationManager = locationManager ?: return null
        if (!localLocationManager.isProviderEnabled(provider)) {
            return null
        }
        if (CacheUseMode.ONLY == cacheUseMode) {
            return localLocationManager.getLastKnownLocation(provider)
        } else if (CacheUseMode.FIRST == cacheUseMode) {
            localLocationManager.getLastKnownLocation(provider)?.let {
                return it
            }
        }
        return suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            LocationManagerCompat.getCurrentLocation(
                localLocationManager,
                provider,
                cancellationSignal,
                ContextCompat.getMainExecutor(Scaffold.context)
            ) { location -> continuation.resume(location) }
            continuation.invokeOnCancellation {
                cancellationSignal.cancel()
            }
        }
    }

    /**
     * 从指定的提供程序获取当前位置
     *
     * 依次遍历所有指定的提供程序，直到获取到位置数据为止，如果所有提供程序都无法获取到位置数据，则返回NULL。
     *
     * @see LocationManagerCompat.getCurrentLocation
     */
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    suspend fun getCurrentLocation(
        providers: Array<String>,
        @CacheUseMode cacheUseMode: Int = CacheUseMode.IGNORE
    ): Location? {
        for (provider in providers) {
            getCurrentLocation(provider, cacheUseMode)?.let {
                return it
            }
        }
        return null
    }

}