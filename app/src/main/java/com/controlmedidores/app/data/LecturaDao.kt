package com.controlmedidores.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LecturaDao {
    @Query("SELECT * FROM lecturas WHERE medidorId = :medidorId ORDER BY fecha DESC")
    fun obtenerPorMedidor(medidorId: Long): Flow<List<Lectura>>

    @Query("SELECT * FROM lecturas WHERE medidorId = :medidorId ORDER BY fecha DESC LIMIT 1")
    suspend fun obtenerUltima(medidorId: Long): Lectura?

    @Insert
    suspend fun insertar(lectura: Lectura)
}
