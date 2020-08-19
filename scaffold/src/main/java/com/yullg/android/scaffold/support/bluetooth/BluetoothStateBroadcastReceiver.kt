package com.yullg.android.scaffold.support.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import java.util.*

abstract class BluetoothStateBroadcastReceiver : BroadcastReceiver() {

    final override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (!Objects.equals(BluetoothAdapter.ACTION_STATE_CHANGED, intent.action)) return
        val state = when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
            BluetoothAdapter.STATE_TURNING_ON -> BluetoothState.TURNING_ON
            BluetoothAdapter.STATE_ON -> BluetoothState.ON
            BluetoothAdapter.STATE_TURNING_OFF -> BluetoothState.TURNING_OFF
            BluetoothAdapter.STATE_OFF -> BluetoothState.OFF
            else -> BluetoothState.UNKNOWN
        }
        val previousState =
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1)) {
                BluetoothAdapter.STATE_TURNING_ON -> BluetoothState.TURNING_ON
                BluetoothAdapter.STATE_ON -> BluetoothState.ON
                BluetoothAdapter.STATE_TURNING_OFF -> BluetoothState.TURNING_OFF
                BluetoothAdapter.STATE_OFF -> BluetoothState.OFF
                else -> BluetoothState.UNKNOWN
            }
        onStateChanged(context, state, previousState)
    }

    protected abstract fun onStateChanged(
        context: Context,
        state: BluetoothState,
        previousState: BluetoothState
    )

    companion object {

        fun register(context: Context, receiver: BroadcastReceiver) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            context.registerReceiver(receiver, intentFilter)
        }

        fun unregister(context: Context, receiver: BroadcastReceiver) {
            context.unregisterReceiver(receiver)
        }

    }

}

enum class BluetoothState { TURNING_ON, ON, TURNING_OFF, OFF, UNKNOWN }