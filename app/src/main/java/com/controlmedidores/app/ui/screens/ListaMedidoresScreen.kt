package com.controlmedidores.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.controlmedidores.app.data.Medidor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaMedidoresScreen(
    medidores: List<Medidor>,
    medidoresVencidos: List<Medidor>,
    onAgregarMedidor: (String) -> Unit,
    onEliminarMedidor: (Medidor) -> Unit,
    onAbrirMedidor: (Medidor) -> Unit
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    val formato = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val idsVencidos = remember(medidoresVencidos) { medidoresVencidos.map { it.id }.toSet() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis medidores") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar medidor")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (medidoresVencidos.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(modifier = Modifier.padding(14.dp)) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Column {
                            val titulo = if (medidoresVencidos.size == 1) {
                                "Toca hacer una lectura"
                            } else {
                                "Toca hacer ${medidoresVencidos.size} lecturas"
                            }
                            Text(titulo, color = Color(0xFFE65100), style = MaterialTheme.typography.titleSmall)
                            medidoresVencidos.forEach { m ->
                                Text(
                                    "${m.nombre} — desde el ${formato.format(Date(m.proximaLectura))}",
                                    color = Color(0xFFE65100),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            if (medidores.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Aún no tienes medidores registrados.")
                    Text("Toca el botón + para agregar el primero (nombre del medidor o persona).")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(medidores, key = { it.id }) { medidor ->
                        val vencido = idsVencidos.contains(medidor.id)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAbrirMedidor(medidor) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Text(medidor.nombre, style = MaterialTheme.typography.titleMedium)
                                    Text("Última lectura: ${medidor.ultimaLectura} kW")
                                    Text("Próxima lectura: ${formato.format(Date(medidor.proximaLectura))}")
                                    if (vencido) {
                                        Text(
                                            "Lectura pendiente",
                                            color = Color(0xFFC62828),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                IconButton(onClick = { onEliminarMedidor(medidor) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        var nombre by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Nuevo medidor") },
            text = {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del medidor o persona") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onAgregarMedidor(nombre)
                    nombre = ""
                    mostrarDialogo = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}
