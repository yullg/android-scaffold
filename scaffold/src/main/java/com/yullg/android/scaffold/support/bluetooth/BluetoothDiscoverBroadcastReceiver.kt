package com.yullg.android.scaffold.support.bluetooth

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import java.util.*

abstract class BluetoothDiscoverBroadcastReceiver : BroadcastReceiver() {

    final override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (!Objects.equals(BluetoothDevice.ACTION_FOUND, intent.action)) return
        val bluetoothDevice: BluetoothDevice =
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
        val bluetoothClass: BluetoothClass =
            intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS)!!
        val name: String? = intent.getStringExtra(BluetoothDevice.EXTRA_NAME)
        val rssi: Short? = if (intent.hasExtra(BluetoothDevice.EXTRA_RSSI)) {
            intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, 0)
        } else {
            null
        }
        onDeviceFound(
            context,
            FoundBluetoothDevice(bluetoothDevice, bluetoothClass, name, rssi)
        )
    }

    protected abstract fun onDeviceFound(
        context: Context,
        foundBluetoothDevice: FoundBluetoothDevice
    )

    companion object {

        fun register(context: Context, receiver: BroadcastReceiver) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
            context.registerReceiver(receiver, intentFilter)
        }

        fun unregister(context: Context, receiver: BroadcastReceiver) {
            context.unregisterReceiver(receiver)
        }

    }

}

data class FoundBluetoothDevice(
    val bluetoothDevice: BluetoothDevice,
    val bluetoothClass: BluetoothClass,
    val name: String?,
    val rssi: Short?
)