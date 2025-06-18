package com.waoos.remotecontrol.presentation.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.waoos.remotecontrol.R

class DeviceAdapter(private val onItemClick: (BluetoothDevice) -> Unit) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    private val devices: MutableList<BluetoothDevice> = mutableListOf()
    private var selectedDevice: BluetoothDevice? = null

    // Método para actualizar la lista de dispositivos
    fun updateDeviceList(newDevices: List<BluetoothDevice>) {
        // Evitar agregar dispositivos duplicados
        val filteredDevices = newDevices.filter { newDevice ->
            devices.none { it.address == newDevice.address }
        }
        // Si deseas reemplazar toda la lista:
        devices.addAll(filteredDevices) // Agregar los nuevos dispositivos
        notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
    }
    fun deleteDeviceList(newDevices: List<BluetoothDevice>) {
        devices.clear()  // Limpiar la lista actual de dispositivos
        notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
    }

    fun getSelectedDevice(): BluetoothDevice? {
        return selectedDevice
    }

    // Método para crear un ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        return DeviceViewHolder(view)
    }

    // Método para vincular los datos a la vista

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.bind(device)

        // Manejar selección de item
        holder.itemView.setOnClickListener {
            selectedDevice = device
            onItemClick(device)
            notifyDataSetChanged() // Actualizar la lista para reflejar la selección
        }

        // Cambiar el fondo si está seleccionado
        holder.itemView.setBackgroundColor(
            if (selectedDevice == device)
                ContextCompat.getColor(holder.itemView.context, R.color.selectedItemColor)
            else
                ContextCompat.getColor(holder.itemView.context, android.R.color.transparent)
        )
    }

    // Método para obtener el número de elementos en la lista
    override fun getItemCount(): Int {
        return devices.size
    }

    // ViewHolder que representa cada item de dispositivo
    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceName: TextView = itemView.findViewById(R.id.tvNombre)
        private val deviceAddress: TextView = itemView.findViewById(R.id.tvDireccion)

        // Vincula los datos del dispositivo a los elementos de la vista
        fun bind(device: BluetoothDevice) {
            //deviceName.text = device.name
            deviceName.text = device.name ?: "Desconocido"
            deviceAddress.text = device.address
        }
    }
}
