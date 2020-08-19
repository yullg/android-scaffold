package com.yullg.android.scaffold.support.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.yullg.android.scaffold.app.Scaffold

object BluetoothSupport {

    val hasBluetoothFeature: Boolean by lazy {
        Scaffold.context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    }

    val hasBluetoothLEFeature: Boolean by lazy {
        Scaffold.context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    val bluetoothAdapter: BluetoothAdapter? by lazy {
        ContextCompat.getSystemService(Scaffold.context, BluetoothManager::class.java)?.adapter
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled ?: false

    @SuppressLint("MissingPermission")
    fun enableBluetooth(activity: Activity, requestCode: Int) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(intent, requestCode)
    }

    @SuppressLint("MissingPermission")
    fun enableBluetoothDiscoverability(
        activity: Activity,
        requestCode: Int,
        duration: Int? = null
    ) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        if (duration != null) {
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration)
        }
        activity.startActivityForResult(intent, requestCode)
    }

}