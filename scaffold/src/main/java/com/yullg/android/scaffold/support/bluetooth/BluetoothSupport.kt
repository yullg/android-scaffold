package com.yullg.android.scaffold.support.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.yullg.android.scaffold.app.Scaffold

/**
 * 提供设备蓝牙相关功能的支持
 */
object BluetoothSupport {

    /**
     * 判断此设备是否有BLUETOOTH特性
     *
     * @see PackageManager.FEATURE_BLUETOOTH
     */
    val hasBluetoothFeature: Boolean by lazy {
        Scaffold.context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    }

    /**
     * 判断此设备是否有BLUETOOTH_LE特性
     *
     * @see PackageManager.FEATURE_BLUETOOTH_LE
     */
    val hasBluetoothLEFeature: Boolean by lazy {
        Scaffold.context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    /**
     * 获取此设备的BLUETOOTH Adapter
     *
     * @see BluetoothManager.getAdapter
     */
    val bluetoothAdapter: BluetoothAdapter? by lazy {
        ContextCompat.getSystemService(Scaffold.context, BluetoothManager::class.java)?.adapter
    }

    /**
     * 如果蓝牙当前已启用并可以使用，则返回true。
     *
     * @see BluetoothAdapter.isEnabled
     */
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled ?: false

    /**
     * 显示允许用户打开蓝牙的系统Activity
     *
     * @see BluetoothAdapter.ACTION_REQUEST_ENABLE
     */
    @SuppressLint("MissingPermission")
    fun enableBluetooth(activity: Activity, requestCode: Int) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * 显示请求可发现模式的系统Activity。如果蓝牙当前未启用，此Activity还将请求用户打开蓝牙。
     *
     * @see BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
     */
    @SuppressLint("MissingPermission")
    fun enableBluetoothDiscoverability(
        activity: Activity,
        requestCode: Int,
        duration: Int? = null
    ) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        if (duration != null) {
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration)
        }
        activity.startActivityForResult(intent, requestCode)
    }

}