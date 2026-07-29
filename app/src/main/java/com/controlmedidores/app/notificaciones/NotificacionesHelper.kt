package com.controlmedidores.app.notificaciones

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Encargado de crear el canal de notificaciones y programar/cancelar la
 * alarma que avisa "te toca leer los medidores" en la fecha de la próxima
 * lectura de cada medidor. La alarma se dispara aunque la app esté cerrada.
 */
object NotificacionesHelper {

    const val CANAL_ID = "recordatorio_lectura"
    private const val EXTRA_MEDIDOR_ID = "medidor_id"
    private const val EXTRA_MEDIDOR_NOMBRE = "medidor_nombre"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val canalExistente = manager.getNotificationChannel(CANAL_ID)
            if (canalExistente == null) {
                val canal = NotificationChannel(
                    CANAL_ID,
                    "Recordatorio de lectura",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Avisa cuando toca leer un medidor de luz"
                }
                manager.createNotificationChannel(canal)
            }
        }
    }

    /**
     * Programa la alarma para el medidor indicado en la fecha/hora dada
     * (milisegundos desde epoch). Si ya existía una alarma para ese medidor,
     * la reemplaza.
     */
    fun programar(context: Context, medidorId: Long, nombreMedidor: String, fechaMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_MEDIDOR_ID, medidorId)
            putExtra(EXTRA_MEDIDOR_NOMBRE, nombreMedidor)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medidorId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fechaMillis, pendingIntent)
        } catch (e: SecurityException) {
            // Si el dispositivo bloquea alarmas exactas, se reintenta con una alarma normal.
            alarmManager.set(AlarmManager.RTC_WAKEUP, fechaMillis, pendingIntent)
        }
    }

    fun cancelar(context: Context, medidorId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medidorId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun leerMedidorId(intent: Intent): Long = intent.getLongExtra(EXTRA_MEDIDOR_ID, -1)
    fun leerMedidorNombre(intent: Intent): String = intent.getStringExtra(EXTRA_MEDIDOR_NOMBRE) ?: "Medidor"
}
