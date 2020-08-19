package com.yullg.android.scaffold.support.location

import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.yullg.android.scaffold.app.Scaffold

object LocationSupport {

    val locationManager: LocationManager? by lazy {
        ContextCompat.getSystemService(Scaffold.context, LocationManager::class.java)
    }

    fun isLocationEnabled(): Boolean = locationManager?.run {
        LocationManagerCompat.isLocationEnabled(this)
    } ?: false

}