package com.waoos.remotecontrol.presentation.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.waoos.remotecontrol.R
import com.waoos.remotecontrol.presentation.common.ConnectedSocketManager
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ServoControlFragment : Fragment() {

    private lateinit var deviceNameTextView: TextView
    private lateinit var deviceAddressTextView: TextView
    private lateinit var seekBarServo: SeekBar
    private lateinit var angleTextView: TextView
    private lateinit var btnOpen: Button
    private lateinit var btnClose: Button
    private var bluetoothSocket: BluetoothSocket? = null
    private var currentAngle = 0

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

        bluetoothSocket = ConnectedSocketManager.bluetoothSocket

        if (bluetoothSocket != null && bluetoothSocket!!.isConnected) {
            deviceNameTextView.text = bluetoothSocket!!.remoteDevice.name ?: "Desconocido"
            deviceAddressTextView.text = bluetoothSocket!!.remoteDevice.address ?: "-"
        } else {
            deviceNameTextView.text = "No conectado"
            deviceAddressTextView.text = "-"
        }

        angleTextView.text = "Ángulo: $currentAngle°"

        seekBarServo.max = 100
        seekBarServo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentAngle = progress
                angleTextView.text = "Ángulo: $currentAngle°"
                sendCommand("S$currentAngle")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnOpen.setOnClickListener {
            currentAngle = 100
            angleTextView.text = "Ángulo: $currentAngle°"
            sendCommand("A")
        }

        btnClose.setOnClickListener {
            currentAngle = 0
            angleTextView.text = "Ángulo: $currentAngle°"
            sendCommand("N")
        }

        checkNotificationPermission()
        createNotificationChannel()
        listenForIncomingData()
    }

    private fun sendCommand(message: String) {
        try {
            bluetoothSocket?.outputStream?.write(message.toByteArray())
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Error al enviar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "BluetoothEvents"
            val descriptionText = "Canal para eventos del timbre Bluetooth"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("doorbell_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showDoorbellNotification() {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val message = "Alguien tocó la puerta a las: $time"

        val notification = NotificationCompat.Builder(requireContext(), "doorbell_channel")
            .setSmallIcon(R.drawable.ic_notif) // Asegúrate de tener este ícono
            .setContentTitle("Timbre Bluetooth")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(requireContext()).notify(1001, notification)
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
        }

    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    private fun listenForIncomingData() {
        val inputStream = bluetoothSocket?.inputStream ?: return

        Thread {
            val buffer = ByteArray(1024)
            var bytes: Int

            while (!Thread.interrupted()) {
                try {
                    bytes = inputStream.read(buffer)
                    val incomingMessage = String(buffer, 0, bytes).trim()

                    if (incomingMessage.contains("z", ignoreCase = true)) {
                        requireActivity().runOnUiThread {
                            showDoorbellNotification()
                        }
                    }

                } catch (e: IOException) {
                    break
                }
            }
        }.start()
    }
}
