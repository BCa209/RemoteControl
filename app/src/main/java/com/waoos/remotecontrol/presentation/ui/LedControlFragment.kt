package com.waoos.remotecontrol.presentation.ui

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.waoos.remotecontrol.R
import com.waoos.remotecontrol.presentation.common.ConnectedSocketManager
import java.io.IOException

class LedControlFragment : Fragment() {

    private lateinit var deviceNameTextView: TextView
    private lateinit var deviceAddressTextView: TextView
    private lateinit var switchControl: Switch
    private lateinit var buzzerButton: ImageView
    private var bluetoothSocket: BluetoothSocket? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_led_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar componentes
        deviceNameTextView = view.findViewById(R.id.deviceName)
        deviceAddressTextView = view.findViewById(R.id.deviceAddress)
        switchControl = view.findViewById(R.id.switchControl)
        buzzerButton = view.findViewById(R.id.buzzerButton)

        // Obtener el socket Bluetooth global desde ConnectedSocketManager
        bluetoothSocket = ConnectedSocketManager.bluetoothSocket

        // Mostrar datos del dispositivo Bluetooth conectado
        if (bluetoothSocket != null && bluetoothSocket?.isConnected == true) {
            deviceNameTextView.text = bluetoothSocket?.remoteDevice?.name ?: "Dispositivo desconocido"
            deviceAddressTextView.text = bluetoothSocket?.remoteDevice?.address ?: "Dirección MAC desconocida"
        } else {
            deviceNameTextView.text = "No conectado"
            deviceAddressTextView.text = "-"
        }

        // Configurar el interruptor para encender/apagar el LED
        switchControl.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendCommand("1") // Encender el LED
            } else {
                sendCommand("0") // Apagar el LED
            }
        }

        // Configurar el botón para el buzzer
        buzzerButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    sendCommand("B") // Activar buzzer
                }
                MotionEvent.ACTION_UP -> {
                    sendCommand("b") // Desactivar buzzer
                }
            }
            true
        }
    }

    private fun sendCommand(message: String) {
        if (bluetoothSocket != null && bluetoothSocket?.isConnected == true) {
            try {
                bluetoothSocket?.outputStream?.write(message.toByteArray())
            } catch (e: IOException) {
                Toast.makeText(requireActivity(), "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireActivity(), "No hay conexión Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // No cerrar el socket aquí, ya que es un recurso global compartido
    }

    override fun onStart() {
        super.onStart()
        // Verificar si el socket ya está conectado
        if (bluetoothSocket == null || bluetoothSocket?.isConnected == false) {
            Toast.makeText(requireActivity(), "No hay conexión activa con el dispositivo", Toast.LENGTH_SHORT).show()
        }
    }
}


