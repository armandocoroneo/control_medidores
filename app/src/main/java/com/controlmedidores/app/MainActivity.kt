package com.controlmedidores.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.controlmedidores.app.data.Medidor
import com.controlmedidores.app.ui.screens.DetalleMedidorScreen
import com.controlmedidores.app.ui.screens.ListaMedidoresScreen
import com.controlmedidores.app.ui.theme.ControlMedidoresTheme
import com.controlmedidores.app.viewmodel.MedidorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MedidorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ControlMedidoresTheme {
                AppNavegacion(viewModel)
            }
        }
    }
}

@Composable
fun AppNavegacion(viewModel: MedidorViewModel) {
    val medidores by viewModel.medidores.collectAsState()
    val medidoresVencidos by viewModel.medidoresVencidos.collectAsState()
    var medidorSeleccionado by remember { mutableStateOf<Medidor?>(null) }

    val seleccionado = medidorSeleccionado
    if (seleccionado == null) {
        ListaMedidoresScreen(
            medidores = medidores,
            medidoresVencidos = medidoresVencidos,
            onAgregarMedidor = { nombre -> viewModel.crearMedidor(nombre) },
            onEliminarMedidor = { medidor -> viewModel.eliminarMedidor(medidor) },
            onAbrirMedidor = { medidor ->
                viewModel.seleccionarMedidor(medidor.id)
                medidorSeleccionado = medidor
            }
        )
    } else {
        // Mantiene el medidor sincronizado con la lista (por si cambia su última lectura)
        val medidorActualizado = medidores.find { it.id == seleccionado.id } ?: seleccionado
        val lecturas by viewModel.lecturasDelSeleccionado.collectAsState(initial = emptyList())
        val prorrateos by viewModel.prorrateosDelSeleccionado.collectAsState(initial = emptyList())

        DetalleMedidorScreen(
            medidor = medidorActualizado,
            lecturas = lecturas,
            prorrateos = prorrateos,
            obtenerPromedioDiario = { viewModel.obtenerPromedioDiario(medidorActualizado) },
            onVolver = { medidorSeleccionado = null },
            onRegistrarLectura = { nuevaLectura, precioPorKw, nota ->
                viewModel.registrarLectura(medidorActualizado, nuevaLectura, precioPorKw, nota)
            },
            onRegistrarProrrateo = { dias, precioPorKw, consumoEstimado, nota ->
                viewModel.registrarProrrateo(medidorActualizado.id, dias, precioPorKw, consumoEstimado, nota)
            }
        )
    }
}
