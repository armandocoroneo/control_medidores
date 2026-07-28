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

    fun obtenerLecturas(medidorId: Long): Flow<List<Lectura>> = lecturaDao.obtenerPorMedidor(medidorId)

    fun obtenerProrrateos(medidorId: Long): Flow<List<Prorrateo>> = prorrateoDao.obtenerPorMedidor(medidorId)

    suspend fun crearMedidor(nombre: String) {
        val ahora = System.currentTimeMillis()
        medidorDao.insertar(
            Medidor(
                nombre = nombre,
                ultimaLectura = 0.0,
                fechaUltimaLectura = ahora,
                proximaLectura = sumarUnMes(ahora)
            )
        )
    }

    suspend fun eliminarMedidor(medidor: Medidor) {
        medidorDao.eliminar(medidor)
    }

    /**
     * Registra una nueva lectura para el medidor indicado.
     * Comprueba automáticamente la lectura anterior guardada, calcula el
     * consumo y el costo con el precio por kW ingresado, y actualiza el
     * medidor con la nueva lectura y la próxima fecha de lectura (un mes después).
     */
    suspend fun registrarLectura(
        medidor: Medidor,
        lecturaNueva: Double,
        precioPorKw: Double,
        nota: String?
    ) {
        val ahora = System.currentTimeMillis()
        val dias = diasEntre(medidor.fechaUltimaLectura, ahora).coerceAtLeast(1)
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
                fecha = ahora,
                nota = nota
            )
        )
        medidorDao.actualizar(
            medidor.copy(
                ultimaLectura = lecturaNueva,
                fechaUltimaLectura = ahora,
                proximaLectura = sumarUnMes(ahora)
            )
        )
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

        fun sumarUnMes(fechaMillis: Long): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = fechaMillis
            cal.add(Calendar.MONTH, 1)
            return cal.timeInMillis
        }
    }
}
