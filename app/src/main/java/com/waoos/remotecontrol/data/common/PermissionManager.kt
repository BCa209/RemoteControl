package com.waoos.remotecontrol.data.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.waoos.remotecontrol.presentation.ui.MainActivity

class PermissionManager {

    companion object {
        // Definir los permisos requeridos
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        // Método para verificar si los permisos están concedidos
        fun hasPermissions(context: Context): Boolean {
            return REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        }

        // Método para solicitar permisos
        fun requestPermissions(activity: MainActivity) {
            ActivityCompat.requestPermissions(
                activity,
                REQUIRED_PERMISSIONS,
                activity.PERMISSION_REQUEST_CODE
            )
        }
    }
}