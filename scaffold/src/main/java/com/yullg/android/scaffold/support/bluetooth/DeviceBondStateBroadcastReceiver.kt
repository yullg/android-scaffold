package com.yullg.android.scaffold.support.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import java.util.*

abstract class DeviceBondStateBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (!Objects.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED, intent.action)) return
        val bluetoothDevice: BluetoothDevice =
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
        val state = when (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)) {
            BluetoothDevice.BOND_NONE -> BondState.NONE
            BluetoothDevice.BOND_BONDING -> BondState.BONDING
            BluetoothDevice.BOND_BONDED -> BondState.BONDED
            else -> BondState.UNKNOWN
        }
        val previousState =
            when (intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)) {
                BluetoothDevice.BOND_NONE -> BondState.NONE
                BluetoothDevice.BOND_BONDING -> BondState.BONDING
                BluetoothDevice.BOND_BONDED -> BondState.BONDED
                else -> BondState.UNKNOWN
            }
        onBondStateChanged(context, DeviceBondState(bluetoothDevice, state, previousState))
    }

    protected abstract fun onBondStateChanged(context: Context, deviceBondState: DeviceBondState)

    companion object {

        fun register(context: Context, receiver: BroadcastReceiver) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            context.registerReceiver(receiver, intentFilter)
        }

        fun unregister(context: Context, receiver: BroadcastReceiver) {
            context.unregisterReceiver(receiver)
        }

    }

}

enum class BondState { NONE, BONDING, BONDED, UNKNOWN }

data class DeviceBondState(
    val bluetoothDevice: BluetoothDevice,
    val state: BondState,
    val previousState: BondState
)