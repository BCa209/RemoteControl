package com.waoos.remotecontrol.presentation.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.waoos.remotecontrol.R

object NotificationHelper {

    private const val CHANNEL_ID = "Z_CHANNEL"
    private const val CHANNEL_NAME = "Z Alert Channel"
    private const val NOTIFICATION_ID = 1

    fun sendZNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal de notificaciones (solo para Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificación de alerta 'z'"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif) // ✅ Asegúrate de tener este ícono en res/drawable
            .setContentTitle("Alerta recibida")
            .setContentText("Alguien tocó la puerta. Revisa quien es por favor")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
