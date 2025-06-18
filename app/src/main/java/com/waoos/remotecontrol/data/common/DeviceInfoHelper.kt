package com.waoos.remotecontrol.data.common

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build

class DeviceInfoHelper(private val context: Context) {

    fun getDeviceInfo(): Map<String, String> {
        val bluetoothMac = getBluetoothMac()
        val wifiMac = getWifiMac()

        return mapOf(
            "device_model" to Build.MODEL,
            "device_brand" to Build.BRAND,
            "os_version" to Build.VERSION.RELEASE,
            "bluetooth_mac" to (bluetoothMac ?: "Unavailable"),
            "wifi_mac" to (wifiMac ?: "Unavailable")
        )
    }

    private fun getBluetoothMac(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "Unavailable on Android 10+"
        } else {
            BluetoothAdapter.getDefaultAdapter()?.address
        }
    }

    private fun getWifiMac(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "Unavailable on Android 10+"
        } else {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wifiManager.connectionInfo
            info.macAddress
        }
    }
}
