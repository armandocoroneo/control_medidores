package com.controlmedidores.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Registro de facturación prorrateada cuando el inquilino se va antes de la
 * lectura mensual. No modifica la lectura real del medidor: solo estima el
 * consumo según los días ocupados y el promedio diario histórico.
 */
@Entity(
    tableName = "prorrateos",
    foreignKeys = [
        ForeignKey(
            entity = Medidor::class,
            parentColumns = ["id"],
            childColumns = ["medidorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Prorrateo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medidorId: Long,
    val dias: Int,
    val precioPorKw: Double,
    val consumoEstimado: Double,
    val costoTotal: Double,
    val fecha: Long,
    val nota: String? = null
)
