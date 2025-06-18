package com.waoos.remotecontrol.presentation.ui

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BluetoothPairingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        when (action) {
            BluetoothDevice.ACTION_PAIRING_REQUEST -> {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // Handle pairing request if necessary
            }
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)

                when (bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        Toast.makeText(context, "Emparejado con ${device?.name}", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothDevice.BOND_NONE -> {
                        Toast.makeText(context, "Desemparejado de ${device?.name}", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        Toast.makeText(context, "Emparejando con ${device?.name}...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
