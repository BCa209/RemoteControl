package com.waoos.remotecontrol.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.waoos.remotecontrol.R
import com.waoos.remotecontrol.presentation.common.ConnectedSocketManager
import java.io.IOException

class ProfileControlFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var deviceNameTextView: TextView
    private lateinit var deviceAddressTextView: TextView
    private lateinit var btnBack: Button
    private lateinit var btnLogout: Button

    private var deviceName: String? = null
    private var deviceAddress: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        profileImageView = view.findViewById(R.id.profileImageView)
        userEmailTextView = view.findViewById(R.id.userEmailTextView)
        deviceNameTextView = view.findViewById(R.id.deviceNameTextView)
        deviceAddressTextView = view.findViewById(R.id.deviceAddressTextView)
        btnBack = view.findViewById(R.id.btnBack)
        btnLogout = view.findViewById(R.id.btnLogout)

        // Obtener datos del dispositivo Bluetooth desde ConnectedSocketManager
        val bluetoothSocket = ConnectedSocketManager.bluetoothSocket
        if (bluetoothSocket != null && bluetoothSocket.isConnected) {
            deviceNameTextView.text = bluetoothSocket.remoteDevice.name ?: "Dispositivo desconocido"
            deviceAddressTextView.text = bluetoothSocket.remoteDevice.address ?: "Dirección MAC desconocida"
        } else {
            deviceNameTextView.text = "No conectado"
            deviceAddressTextView.text = "-"
        }

        // Recuperar datos del usuario desde FirebaseAuth
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            userEmailTextView.text = it.email ?: "Correo no disponible"
            val photoUrl = it.photoUrl
            if (photoUrl != null) {
                Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .into(profileImageView)
            }
        } ?: run {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }

        // Configurar botón "Atrás"
        btnBack.setOnClickListener {
            disconnectBluetooth() // Desconectar Bluetooth
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Configurar botón "Cerrar sesión"
        btnLogout.setOnClickListener {
            disconnectBluetooth()
            signOut()
        }
    }


    private fun disconnectBluetooth() {
        val bluetoothSocket = ConnectedSocketManager.bluetoothSocket
        bluetoothSocket?.let {
            try {
                if (it.isConnected) {
                    it.close() // Cerrar el socket Bluetooth
                    Toast.makeText(requireContext(), "Dispositivo desconectado", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(requireContext(), "Error al desconectar", Toast.LENGTH_SHORT).show()
            }
        }
        ConnectedSocketManager.bluetoothSocket = null // Liberar el recurso
    }

    private fun signOut() {
        GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            .addOnCompleteListener {
                Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
    }

    companion object {
        fun newInstance(deviceName: String?, deviceAddress: String?): ProfileControlFragment {
            val fragment = ProfileControlFragment()
            val args = Bundle().apply {
                putString("device_name", deviceName)
                putString("device_address", deviceAddress)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
