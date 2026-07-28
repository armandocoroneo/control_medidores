package com.controlmedidores.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Azul = Color(0xFF1565C0)
private val AzulOscuro = Color(0xFF90CAF9)

private val EsquemaClaro = lightColorScheme(primary = Azul)
private val EsquemaOscuro = darkColorScheme(primary = AzulOscuro)

@Composable
fun ControlMedidoresTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colores = if (darkTheme) EsquemaOscuro else EsquemaClaro
    MaterialTheme(colorScheme = colores, content = content)
}
