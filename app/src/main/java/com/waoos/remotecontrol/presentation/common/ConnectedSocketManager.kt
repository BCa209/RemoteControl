package com.waoos.remotecontrol.presentation.common

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.waoos.remotecontrol.presentation.common.NotificationHelper
import java.io.IOException

object ConnectedSocketManager {

    var bluetoothSocket: BluetoothSocket? = null
    private var isListening = false  // para evitar múltiples hilos

    fun setSocket(socket: BluetoothSocket) {
        bluetoothSocket = socket
    }

    fun isConnected(): Boolean {
        return bluetoothSocket?.isConnected == true
    }

    fun sendCommand(message: String): Boolean {
        return try {
            if (isConnected()) {
                bluetoothSocket?.outputStream?.write(message.toByteArray())
                true
            } else {
                false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun closeConnection() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
            isListening = false
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // ✅ Nuevo método para escuchar constantemente
    fun startListening(context: Context) {
        if (isListening || bluetoothSocket == null) return
        isListening = true

        Thread {
            try {
                val inputStream = bluetoothSocket!!.inputStream
                val buffer = ByteArray(1024)

                while (isConnected()) {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val received = String(buffer, 0, bytesRead)
                        Log.d("BluetoothReceiver", "Recibido: $received")

                        if (received.contains("z", ignoreCase = true)) {
                            NotificationHelper.sendZNotification(context)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BluetoothReceiver", "Error en la lectura del socket", e)
                isListening = false
            }
        }.start()
    }
}
