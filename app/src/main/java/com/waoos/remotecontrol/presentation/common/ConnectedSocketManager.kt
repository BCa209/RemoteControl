package com.waoos.remotecontrol.presentation.common

import android.bluetooth.BluetoothSocket
import java.io.IOException

object ConnectedSocketManager {

    // Variable para almacenar el socket Bluetooth compartido
    var bluetoothSocket: BluetoothSocket? = null

    /**
     * Configura el socket Bluetooth.
     * @param socket El socket Bluetooth a compartir.
     */
    fun setSocket(socket: BluetoothSocket) {
        bluetoothSocket = socket
    }

    /**
     * Comprueba si la conexión Bluetooth está activa.
     * @return `true` si el socket está conectado, `false` de lo contrario.
     */
    fun isConnected(): Boolean {
        return bluetoothSocket?.isConnected == true
    }

    /**
     * Envía un mensaje a través del socket Bluetooth.
     * @param message El mensaje a enviar.
     * @return `true` si el mensaje se envió correctamente, `false` si ocurrió un error.
     */
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

    /**
     * Cierra el socket Bluetooth para liberar recursos.
     */
    fun closeConnection() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
