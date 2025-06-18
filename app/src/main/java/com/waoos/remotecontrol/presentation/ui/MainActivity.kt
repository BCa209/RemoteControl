@file:Suppress("DEPRECATION")

package com.waoos.remotecontrol.presentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.waoos.remotecontrol.R
import com.waoos.remotecontrol.data.common.PermissionManager
import com.waoos.remotecontrol.presentation.adapter.DeviceAdapter
import com.waoos.remotecontrol.presentation.common.ConnectedSocketManager
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var switchBluetooth: Switch
    private lateinit var btnRefresh: Button
    private lateinit var btnPair: Button
    private lateinit var btnUnPair: Button
    private lateinit var btnGoPanel: Button
    private lateinit var recyclerViewDevices: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var pairingReceiver: BluetoothPairingReceiver
    private var selectedDevice: BluetoothDevice? = null  // Usar una variable nullable para el dispositivo seleccionado
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var outputStream: OutputStream

    private val REQUEST_NOTIFICATION_PERMISSION = 1001
    private val REQUEST_ENABLE_BT = 1
    val PERMISSION_REQUEST_CODE = 2
    val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa componentes
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        switchBluetooth = findViewById(R.id.switchBluetooth)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnPair = findViewById(R.id.btnPair)
        btnUnPair = findViewById(R.id.btnUnPair)
        btnGoPanel = findViewById(R.id.btnGoPanel)
        recyclerViewDevices = findViewById(R.id.recyclerViewDevices)

        // Configura RecyclerView
        recyclerViewDevices.layoutManager = LinearLayoutManager(this)

        // Crear el adaptador y pasar el callback para manejar la selección
        deviceAdapter = DeviceAdapter { device ->
            selectedDevice = device  // Guardar el dispositivo seleccionado
        }
        recyclerViewDevices.adapter = deviceAdapter

        // Verificar si el dispositivo tiene Bluetooth
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        // Establecer el estado del Switch de Bluetooth
        switchBluetooth.isChecked = bluetoothAdapter.isEnabled
        // Manejo del cambio en el Switch de Bluetooth
        switchBluetooth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableBluetooth()
            } else {
                disableBluetooth()
                // Limpiar el RecyclerView cuando el Bluetooth se apaga
                deviceAdapter.deleteDeviceList(emptyList())
            }
        }
        // **Llamar a la función para configurar los listeners de botones**
        requestNotificationPermission()
        configButtonListener()
    }

    private fun configButtonListener() {
        // Botón para buscar dispositivos
        btnRefresh.setOnClickListener {
            // Limpiar el RecyclerView antes de comenzar la búsqueda
            deviceAdapter.updateDeviceList(emptyList())

            // Verificar y solicitar permisos si es necesario
            if (PermissionManager.hasPermissions(this)) {
                // Mostrar "Buscando..." antes de empezar el escaneo
                Toast.makeText(this, "Buscando...", Toast.LENGTH_SHORT).show()
                startDiscovery()
            } else {
                PermissionManager.requestPermissions(this)
            }
        }

        //Emparejar con Dispositivo
        btnPair.setOnClickListener {
            val selectedDevice = deviceAdapter.getSelectedDevice()
            if (selectedDevice != null) {
                pairDevice(selectedDevice)
            } else {
                Toast.makeText(this, "Seleccione un dispositivo para conectar", Toast.LENGTH_SHORT).show()
            }
        }

        btnUnPair.setOnClickListener {
            val selectedDevice = deviceAdapter.getSelectedDevice()
            if (selectedDevice != null) {
                unpairDevice(selectedDevice)
            } else {
                Toast.makeText(this, "Seleccione un dispositivo para desemparejar", Toast.LENGTH_SHORT).show()
            }
        }
        btnGoPanel.setOnClickListener {
            val selectedDevice = deviceAdapter.getSelectedDevice()

            if (selectedDevice != null) {
                if (selectedDevice.bondState == BluetoothDevice.BOND_BONDED) {
                    try {
                        // Crear el socket
                        val socket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID)

                        // Cancelar el descubrimiento para evitar conflictos durante la conexión
                        bluetoothAdapter.cancelDiscovery()

                        // Conectar el socket
                        socket.connect()

                        // Guardar el socket en el manejador global
                        ConnectedSocketManager.bluetoothSocket = socket
                        ConnectedSocketManager.startListening(applicationContext)

                        // Navegar a ControlActivity
                        val intent = Intent(this, BottomNavigationActivity::class.java)
                        intent.putExtra("device_name", selectedDevice.name)
                        intent.putExtra("device_address", selectedDevice.address)
                        startActivity(intent)
                    } catch (e: IOException) {
                        Toast.makeText(this, "Error al conectar con el dispositivo", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this, "Por favor, empareja el dispositivo primero.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Seleccione un dispositivo para continuar.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun pairDevice(device: BluetoothDevice) {
        if (device.bondState == BluetoothDevice.BOND_NONE) {
            // Intentar emparejar si no está emparejado
            device.createBond()
        } else {
            Toast.makeText(this, "Ya estás emparejado con ${device.name}", Toast.LENGTH_SHORT).show()
        }
    }
    @SuppressLint("MissingPermission")
    private fun unpairDevice(device: BluetoothDevice) {
        try {
            val method = device.javaClass.getMethod("removeBond")
            val result = method.invoke(device)
            Toast.makeText(this, "Desemparejamiento exitoso con ${device.name}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al desemparejar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkBluetoothPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }




    private fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            // Verificar si se tienen los permisos de Bluetooth
            if (checkBluetoothPermissions()) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                // Solicitar permisos si no están concedidos
                PermissionManager.requestPermissions(this)
            }
        }
    }

    private fun disableBluetooth() {
        bluetoothAdapter.disable()
    }

    @SuppressLint("MissingPermission")
    private fun startDiscovery() {
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothReceiver, filter)

        bluetoothAdapter.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                val deviceName = device.name
                val deviceAddress = device.address // Dirección MAC

                // Actualizar la lista de dispositivos
                val newDevices = listOf(device) // Crear una lista con el nuevo dispositivo
                deviceAdapter.updateDeviceList(newDevices) // Actualizar el RecyclerView
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Verificar permisos en tiempo de ejecución
        if (!PermissionManager.hasPermissions(this)) {
            PermissionManager.requestPermissions(this)
        }

        pairingReceiver = BluetoothPairingReceiver()

        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(pairingReceiver, filter)
    }

    private fun navigateToControlActivity() {
        val intent = Intent(this, LedControlFragment::class.java)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(pairingReceiver)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    enableBluetooth()
                } else {
                    Toast.makeText(this, "Se necesitan permisos para continuar", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_NOTIFICATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Desregistrar el receptor cuando la actividad se destruya
        unregisterReceiver(bluetoothReceiver)
    }
}