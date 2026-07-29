package com.controlmedidores.app.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MedidorRepository(
    private val medidorDao: MedidorDao,
    private val lecturaDao: LecturaDao,
    private val prorrateoDao: ProrrateoDao
) {
    fun obtenerMedidores(): Flow<List<Medidor>> = medidorDao.obtenerTodos()

    suspend fun obtenerMedidoresUnaVez(): List<Medidor> = medidorDao.obtenerListaUnaVez()

    fun obtenerLecturas(medidorId: Long): Flow<List<Lectura>> = lecturaDao.obtenerPorMedidor(medidorId)

    fun obtenerProrrateos(medidorId: Long): Flow<List<Prorrateo>> = prorrateoDao.obtenerPorMedidor(medidorId)

    /** Devuelve el medidor recién creado (con su id) para poder programar su alarma. */
    suspend fun crearMedidor(nombre: String): Medidor {
        val ahora = System.currentTimeMillis()
        val nuevo = Medidor(
            nombre = nombre,
            ultimaLectura = 0.0,
            fechaUltimaLectura = ahora,
            proximaLectura = sumarUnMes(ahora)
        )
        val id = medidorDao.insertar(nuevo)
        return nuevo.copy(id = id)
    }

    suspend fun eliminarMedidor(medidor: Medidor) {
        medidorDao.eliminar(medidor)
    }

    /**
     * Registra una lectura para el medidor indicado, en la fecha real que se
     * indique (no siempre "hoy" — por ejemplo si la lectura corresponde al
     * día 5 pero la estás cargando el día 10). Comprueba automáticamente la
     * lectura anterior guardada, calcula el consumo y el costo con el precio
     * por kW ingresado, y actualiza el medidor con la nueva lectura y la
     * próxima fecha de lectura (un mes después de la fecha indicada).
     * Devuelve el medidor actualizado, para reprogramar su alarma.
     */
    suspend fun registrarLectura(
        medidor: Medidor,
        lecturaNueva: Double,
        precioPorKw: Double,
        nota: String?,
        fechaLectura: Long
    ): Medidor {
        val dias = diasEntre(medidor.fechaUltimaLectura, fechaLectura).coerceAtLeast(1)
        val consumo = (lecturaNueva - medidor.ultimaLectura).coerceAtLeast(0.0)
        val costo = consumo * precioPorKw

        lecturaDao.insertar(
            Lectura(
                medidorId = medidor.id,
                lecturaAnterior = medidor.ultimaLectura,
                lecturaNueva = lecturaNueva,
                precioPorKw = precioPorKw,
                consumoKw = consumo,
                costoTotal = costo,
                dias = dias,
                fecha = fechaLectura,
                nota = nota
            )
        )
        val actualizado = medidor.copy(
            ultimaLectura = lecturaNueva,
            fechaUltimaLectura = fechaLectura,
            proximaLectura = sumarUnMes(fechaLectura)
        )
        medidorDao.actualizar(actualizado)
        return actualizado
    }

    /**
     * Registra una facturación prorrateada por días cuando el inquilino se va
     * antes de la lectura mensual. No modifica la lectura real del medidor.
     */
    suspend fun registrarProrrateo(
        medidorId: Long,
        dias: Int,
        precioPorKw: Double,
        consumoEstimado: Double,
        nota: String?
    ) {
        prorrateoDao.insertar(
            Prorrateo(
                medidorId = medidorId,
                dias = dias,
                precioPorKw = precioPorKw,
                consumoEstimado = consumoEstimado,
                costoTotal = consumoEstimado * precioPorKw,
                fecha = System.currentTimeMillis(),
                nota = nota
            )
        )
    }

    /**
     * Promedio diario de consumo basado en la última lectura completa
     * registrada. Devuelve null si aún no hay historial.
     */
    suspend fun promedioDiarioConsumo(medidorId: Long): Double? {
        val ultima = lecturaDao.obtenerUltima(medidorId) ?: return null
        if (ultima.dias <= 0) return null
        return ultima.consumoKw / ultima.dias
    }

    companion object {
        fun diasEntre(desde: Long, hasta: Long): Int =
            TimeUnit.MILLISECONDS.toDays(hasta - desde).toInt()

        /** Suma un mes a la fecha dada y fija la hora a las 9:00 am, para que la alarma suene a una hora razonable. */
        fun sumarUnMes(fechaMillis: Long): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = fechaMillis
            cal.add(Calendar.MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
    }
}
