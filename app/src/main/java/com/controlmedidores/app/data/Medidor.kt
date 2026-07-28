package com.controlmedidores.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un medidor de luz asociado a un nombre o persona.
 * Guarda la última lectura y su fecha para poder comprobarla antes de pedir
 * una nueva, y calcular cuándo toca la próxima lectura (mensual).
 */
@Entity(tableName = "medidores")
data class Medidor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val ultimaLectura: Double = 0.0,
    val fechaUltimaLectura: Long = System.currentTimeMillis(),
    val proximaLectura: Long = System.currentTimeMillis()
)
