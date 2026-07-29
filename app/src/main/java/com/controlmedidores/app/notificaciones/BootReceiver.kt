package com.controlmedidores.app.notificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.controlmedidores.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Cuando el celular se reinicia, Android borra las alarmas programadas.
 * Este receptor las vuelve a programar leyendo la próxima lectura de cada
 * medidor guardado en la base de datos.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val appContext = context.applicationContext
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.obtenerInstancia(appContext)
                val medidores = db.medidorDao().obtenerListaUnaVez()
                val ahora = System.currentTimeMillis()
                medidores.forEach { medidor ->
                    if (medidor.proximaLectura > ahora) {
                        NotificacionesHelper.programar(appContext, medidor.id, medidor.nombre, medidor.proximaLectura)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
