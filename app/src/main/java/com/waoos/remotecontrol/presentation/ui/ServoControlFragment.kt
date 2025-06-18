package com.waoos.remotecontrol.presentation.ui

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.waoos.remotecontrol.R
import com.waoos.remotecontrol.presentation.common.ConnectedSocketManager
import java.io.IOException

class ServoControlFragment : Fragment() {

    private lateinit var deviceNameTextView: TextView
    private lateinit var deviceAddressTextView: TextView
    private lateinit var seekBarServo: SeekBar
    private lateinit var angleTextView: TextView
    private lateinit var btnOpen: Button
    private lateinit var btnClose: Button
    //private lateinit var btnOpen10: Button
    //private lateinit var btnClose10: Button
    private var bluetoothSocket: BluetoothSocket? = null
    private var currentAngle = 0 // Ángulo inicial del servomotor (en grados)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_servo_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar componentes
        deviceNameTextView = view.findViewById(R.id.deviceName)
        deviceAddressTextView = view.findViewById(R.id.deviceAddress)
        seekBarServo = view.findViewById(R.id.seekBarServo)
        angleTextView = view.findViewById(R.id.angleTextView)
        btnOpen = view.findViewById(R.id.btnOpen)
        btnClose = view.findViewById(R.id.btnClose)
        //btnOpen10 = view.findViewById(R.id.btnOpen10)
        //btnClose10 = view.findViewById(R.id.btnClose10)

        // Configurar el socket Bluetooth
        bluetoothSocket = ConnectedSocketManager.bluetoothSocket
        // Obtener datos del dispositivo Bluetooth desde ConnectedSocketManager
        val bluetoothSocket = ConnectedSocketManager.bluetoothSocket
        if (bluetoothSocket != null && bluetoothSocket.isConnected) {
            deviceNameTextView.text = bluetoothSocket.remoteDevice.name ?: "Dispositivo desconocido"
            deviceAddressTextView.text = bluetoothSocket.remoteDevice.address ?: "Dirección MAC desconocida"
        } else {
            deviceNameTextView.text = "No conectado"
            deviceAddressTextView.text = "-"
        }

        // Mostrar el ángulo inicial
        angleTextView.text = "Ángulo: $currentAngle°"

        /* Configurar botones de subir y bajar 10 grados
        btnOpen10.setOnClickListener {
            if (currentAngle < 12) {
                currentAngle += 10
                angleTextView.text = "Ángulo: $currentAngle°"
                sendCommand("U") // Enviar 'U' para subir el servomotor
            }
        }

        btnClose10.setOnClickListener {
            if (currentAngle > 0) {
                currentAngle -= 10
                angleTextView.text = "Ángulo: $currentAngle°"
                sendCommand("D") // Enviar 'D' para bajar el servomotor
            }
        }*/
// Configurar el cambio de SeekBar
        seekBarServo.max = 100 // Establecer el máximo del SeekBar en 100
        seekBarServo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Actualizar el ángulo y el TextView con el valor del SeekBar
                currentAngle = progress
                angleTextView.text = "Ángulo: $currentAngle°"
                sendCommand("S$currentAngle") // Enviar el comando para actualizar el ángulo del servomotor
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        // Configurar botón de abrir hasta 100º
        btnOpen.setOnClickListener {
            currentAngle = 100
            angleTextView.text = "Ángulo: $currentAngle°"
            sendCommand("A") // Comando para abrir hasta 100º
        }

        // Configurar botón de cerrar hasta 0º
        btnClose.setOnClickListener {
            currentAngle = 0
            angleTextView.text = "Ángulo: $currentAngle°"
            sendCommand("N") // Comando para cerrar hasta 0º
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
    }

    override fun onStart() {
        super.onStart()
        if (bluetoothSocket == null || bluetoothSocket?.isConnected == false) {
            Toast.makeText(requireActivity(), "No hay conexión activa con el dispositivo", Toast.LENGTH_SHORT).show()
        }
    }
}
