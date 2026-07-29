package com.controlmedidores.app.notificaciones

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.controlmedidores.app.MainActivity

/**
 * Recibe la alarma programada y muestra la notificación
 * "Oye, te toca leer los medidores" para el medidor correspondiente.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medidorId = NotificacionesHelper.leerMedidorId(intent)
        val nombreMedidor = NotificacionesHelper.leerMedidorNombre(intent)

        NotificacionesHelper.crearCanal(context)

        val intentAbrirApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            medidorId.toInt(),
            intentAbrirApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, NotificacionesHelper.CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Oye, te toca leer los medidores")
            .setContentText("Ya es fecha de tomar la lectura de: $nombreMedidor")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Ya es fecha de tomar la lectura de: $nombreMedidor"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val permisoConcedido = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        if (permisoConcedido) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(medidorId.toInt(), notificacion)
        }
    }
}
