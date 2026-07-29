package com.controlmedidores.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedidorDao {
    @Query("SELECT * FROM medidores ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Medidor>>

    @Query("SELECT * FROM medidores ORDER BY nombre ASC")
    suspend fun obtenerListaUnaVez(): List<Medidor>

    @Query("SELECT * FROM medidores WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Medidor?

    @Insert
    suspend fun insertar(medidor: Medidor): Long

    @Update
    suspend fun actualizar(medidor: Medidor)

    @Delete
    suspend fun eliminar(medidor: Medidor)
}
