package com.example.fashgo.data.dao

import androidx.room.*
import com.example.fashgo.data.entity.Ingreso
import kotlinx.coroutines.flow.Flow

@Dao
interface IngresoDao {
    @Query("SELECT * FROM ingresos ORDER BY fecha DESC")
    fun getAll(): Flow<List<Ingreso>>

    @Query("SELECT * FROM ingresos WHERE productoId = :productoId ORDER BY fecha DESC")
    fun getByProducto(productoId: Long): Flow<List<Ingreso>>

    @Insert
    suspend fun insert(ingreso: Ingreso): Long
}
