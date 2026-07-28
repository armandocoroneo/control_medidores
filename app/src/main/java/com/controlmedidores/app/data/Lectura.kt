package com.controlmedidores.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Historial de lecturas de un medidor. Cada registro guarda la lectura
 * anterior (para trazabilidad), la lectura nueva, el precio por kW vigente
 * en ese momento, el consumo y costo calculados, la fecha y una nota opcional.
 */
@Entity(
    tableName = "lecturas",
    foreignKeys = [
        ForeignKey(
            entity = Medidor::class,
            parentColumns = ["id"],
            childColumns = ["medidorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Lectura(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medidorId: Long,
    val lecturaAnterior: Double,
    val lecturaNueva: Double,
    val precioPorKw: Double,
    val consumoKw: Double,
    val costoTotal: Double,
    val dias: Int,
    val fecha: Long,
    val nota: String? = null
)
