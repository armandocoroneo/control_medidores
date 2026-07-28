package com.controlmedidores.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProrrateoDao {
    @Query("SELECT * FROM prorrateos WHERE medidorId = :medidorId ORDER BY fecha DESC")
    fun obtenerPorMedidor(medidorId: Long): Flow<List<Prorrateo>>

    @Insert
    suspend fun insertar(prorrateo: Prorrateo)
}
