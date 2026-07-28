package com.controlmedidores.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.controlmedidores.app.data.Lectura
import com.controlmedidores.app.data.Medidor
import com.controlmedidores.app.data.Prorrateo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed class ItemHistorial(val fecha: Long) {
    data class DeLectura(val lectura: Lectura) : ItemHistorial(lectura.fecha)
    data class DeProrrateo(val prorrateo: Prorrateo) : ItemHistorial(prorrateo.fecha)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMedidorScreen(
    medidor: Medidor,
    lecturas: List<Lectura>,
    prorrateos: List<Prorrateo>,
    obtenerPromedioDiario: suspend () -> Double?,
    onVolver: () -> Unit,
    onRegistrarLectura: (nuevaLectura: Double, precioPorKw: Double, nota: String?) -> Unit,
    onRegistrarProrrateo: (dias: Int, precioPorKw: Double, consumoEstimado: Double, nota: String?) -> Unit
) {
    var mostrarDialogoLectura by remember { mutableStateOf(false) }
    var mostrarDialogoProrrateo by remember { mutableStateOf(false) }

    val historial = remember(lecturas, prorrateos) {
        (lecturas.map { ItemHistorial.DeLectura(it) } + prorrateos.map { ItemHistorial.DeProrrateo(it) })
            .sortedByDescending { it.fecha }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(medidor.nombre) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Lectura anterior guardada", style = MaterialTheme.typography.labelMedium)
                    Text("${medidor.ultimaLectura} kW", style = MaterialTheme.typography.headlineSmall)
                }
            }

            Spacer(modifier = Modifier.padding(top = 12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { mostrarDialogoLectura = true },
                    modifier = Modifier.weight(1f)
                ) { Text("Registrar lectura") }

                OutlinedButton(
                    onClick = { mostrarDialogoProrrateo = true },
                    modifier = Modifier.weight(1f)
                ) { Text("Salida anticipada") }
            }

            Spacer(modifier = Modifier.padding(top = 16.dp))
            Text("Historial", style = MaterialTheme.typography.titleMedium)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(historial, key = {
                    when (it) {
                        is ItemHistorial.DeLectura -> "l-${it.lectura.id}"
                        is ItemHistorial.DeProrrateo -> "p-${it.prorrateo.id}"
                    }
                }) { item ->
                    val formato = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            when (item) {
                                is ItemHistorial.DeLectura -> {
                                    val lectura = item.lectura
                                    Text(formato.format(Date(lectura.fecha)), style = MaterialTheme.typography.labelSmall)
                                    Text("Anterior: ${lectura.lecturaAnterior} kW  →  Nueva: ${lectura.lecturaNueva} kW")
                                    Text("Consumo: ${lectura.consumoKw} kW  ×  Bs/kW ${lectura.precioPorKw}")
                                    Text(
                                        "Total: Bs ${"%.2f".format(lectura.costoTotal)}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    if (!lectura.nota.isNullOrBlank()) {
                                        Text("Nota: ${lectura.nota}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                is ItemHistorial.DeProrrateo -> {
                                    val p = item.prorrateo
                                    Row {
                                        Text(formato.format(Date(p.fecha)), style = MaterialTheme.typography.labelSmall)
                                        Spacer(modifier = Modifier.padding(start = 6.dp))
                                        Text(
                                            "PRORRATEO",
                                            color = Color(0xFF5E35B1),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Text("${p.dias} días  ×  ${"%.2f".format(p.consumoEstimado)} kW estimados")
                                    Text("Precio: Bs/kW ${p.precioPorKw}")
                                    Text(
                                        "Total: Bs ${"%.2f".format(p.costoTotal)}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    if (!p.nota.isNullOrBlank()) {
                                        Text("Nota: ${p.nota}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoLectura) {
        DialogoNuevaLectura(
            medidor = medidor,
            onCancelar = { mostrarDialogoLectura = false },
            onConfirmar = { nueva, precio, nota ->
                onRegistrarLectura(nueva, precio, nota)
                mostrarDialogoLectura = false
            }
        )
    }

    if (mostrarDialogoProrrateo) {
        DialogoSalidaAnticipada(
            medidor = medidor,
            obtenerPromedioDiario = obtenerPromedioDiario,
            onCancelar = { mostrarDialogoProrrateo = false },
            onConfirmar = { dias, precio, consumo, nota ->
                onRegistrarProrrateo(dias, precio, consumo, nota)
                mostrarDialogoProrrateo = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoNuevaLectura(
    medidor: Medidor,
    onCancelar: () -> Unit,
    onConfirmar: (nueva: Double, precio: Double, nota: String?) -> Unit
) {
    var lecturaNueva by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nueva lectura — ${medidor.nombre}") },
        text = {
            Column {
                Text("Lectura anterior: ${medidor.ultimaLectura} kW", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.padding(top = 8.dp))
                OutlinedTextField(
                    value = lecturaNueva,
                    onValueChange = { lecturaNueva = it },
                    label = { Text("Lectura nueva (kW)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio por kW") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota (opcional)") },
                    singleLine = true
                )
                if (error != null) {
                    Text(error!!, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val nuevaValor = lecturaNueva.replace(",", ".").toDoubleOrNull()
                val precioValor = precio.replace(",", ".").toDoubleOrNull()
                when {
                    nuevaValor == null || precioValor == null -> error = "Ingresa números válidos"
                    nuevaValor < medidor.ultimaLectura -> error =
                        "La lectura nueva no puede ser menor a la anterior (${medidor.ultimaLectura})"
                    else -> onConfirmar(nuevaValor, precioValor, nota.ifBlank { null })
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoSalidaAnticipada(
    medidor: Medidor,
    obtenerPromedioDiario: suspend () -> Double?,
    onCancelar: () -> Unit,
    onConfirmar: (dias: Int, precio: Double, consumoEstimado: Double, nota: String?) -> Unit
) {
    var promedioDiario by remember { mutableStateOf<Double?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(medidor.id) {
        promedioDiario = obtenerPromedioDiario()
        cargando = false
    }

    var dias by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var consumoManual by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Salida anticipada — ${medidor.nombre}") },
        text = {
            Column {
                Text(
                    "Prorrateo del consumo estimado según los días que vivió ahí el inquilino.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.padding(top = 8.dp))

                if (cargando) {
                    Text("Calculando promedio histórico…", style = MaterialTheme.typography.bodySmall)
                } else if (promedioDiario != null) {
                    Text(
                        "Promedio diario calculado del historial: ${"%.3f".format(promedioDiario)} kW/día",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1565C0)
                    )
                } else {
                    Text(
                        "Aún no hay historial de lecturas: ingresa tú el consumo estimado del mes completo.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = consumoManual,
                        onValueChange = { consumoManual = it },
                        label = { Text("Consumo estimado del mes (kW)") },
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = dias,
                    onValueChange = { dias = it },
                    label = { Text("Días que ocupó este periodo") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio por kW vigente") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota (opcional)") },
                    singleLine = true
                )
                if (error != null) {
                    Text(error!!, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val diasValor = dias.toIntOrNull()
                val precioValor = precio.replace(",", ".").toDoubleOrNull()
                val diarioEfectivo = promedioDiario
                    ?: consumoManual.replace(",", ".").toDoubleOrNull()?.div(30.0)

                when {
                    diasValor == null || diasValor <= 0 -> error = "Ingresa un número de días válido"
                    precioValor == null -> error = "Ingresa un precio por kW válido"
                    diarioEfectivo == null -> error = "Ingresa el consumo estimado del mes"
                    else -> {
                        val consumoEstimado = diarioEfectivo * diasValor
                        onConfirmar(diasValor, precioValor, consumoEstimado, nota.ifBlank { null })
                    }
                }
            }) { Text("Calcular y guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
