package com.waoos.remotecontrol.presentation.ui

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BluetoothReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
            val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
            when (device.bondState) {
                BluetoothDevice.BOND_BONDING -> {
                    Toast.makeText(context, "Emparejando con ${device.name}...", Toast.LENGTH_SHORT).show()
                }
                BluetoothDevice.BOND_BONDED -> {
                    Toast.makeText(context, "Emparejado exitosamente con ${device.name}", Toast.LENGTH_SHORT).show()
                }
                BluetoothDevice.BOND_NONE -> {
                    Toast.makeText(context, "Emparejamiento cancelado con ${device.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
