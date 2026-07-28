package com.controlmedidores.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.controlmedidores.app.data.AppDatabase
import com.controlmedidores.app.data.Lectura
import com.controlmedidores.app.data.Medidor
import com.controlmedidores.app.data.MedidorRepository
import com.controlmedidores.app.data.Prorrateo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedidorViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio: MedidorRepository

    init {
        val db = AppDatabase.obtenerInstancia(application)
        repositorio = MedidorRepository(db.medidorDao(), db.lecturaDao(), db.prorrateoDao())
    }

    val medidores: StateFlow<List<Medidor>> = repositorio.obtenerMedidores()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    /** Medidores cuya próxima lectura ya venció: para el aviso al abrir la app. */
    val medidoresVencidos: StateFlow<List<Medidor>> = medidores
        .map { lista ->
            val ahora = System.currentTimeMillis()
            lista.filter { it.proximaLectura <= ahora }
        }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    private val medidorSeleccionadoId = MutableStateFlow<Long?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val lecturasDelSeleccionado: Flow<List<Lectura>> = medidorSeleccionadoId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repositorio.obtenerLecturas(id)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val prorrateosDelSeleccionado: Flow<List<Prorrateo>> = medidorSeleccionadoId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repositorio.obtenerProrrateos(id)
    }

    fun seleccionarMedidor(id: Long) {
        medidorSeleccionadoId.value = id
    }

    fun crearMedidor(nombre: String) {
        if (nombre.isBlank()) return
        viewModelScope.launch { repositorio.crearMedidor(nombre.trim()) }
    }

    fun eliminarMedidor(medidor: Medidor) {
        viewModelScope.launch { repositorio.eliminarMedidor(medidor) }
    }

    fun registrarLectura(medidor: Medidor, lecturaNueva: Double, precioPorKw: Double, nota: String?) {
        viewModelScope.launch {
            repositorio.registrarLectura(medidor, lecturaNueva, precioPorKw, nota)
        }
    }

    /**
     * Calcula el prorrateo (sin guardar) para mostrarlo antes de confirmar.
     * Devuelve null si falta el promedio diario y no se indicó consumo manual.
     */
    suspend fun calcularProrrateo(
        medidor: Medidor,
        dias: Int,
        precioPorKw: Double,
        consumoMensualManual: Double?
    ): Double? {
        val promedioDiario = repositorio.promedioDiarioConsumo(medidor.id)
            ?: consumoMensualManual?.div(30.0)
            ?: return null
        return promedioDiario * dias
    }

    suspend fun obtenerPromedioDiario(medidor: Medidor): Double? {
        return repositorio.promedioDiarioConsumo(medidor.id)
    }

    fun registrarProrrateo(medidorId: Long, dias: Int, precioPorKw: Double, consumoEstimado: Double, nota: String?) {
        viewModelScope.launch {
            repositorio.registrarProrrateo(medidorId, dias, precioPorKw, consumoEstimado, nota)
        }
    }
}
