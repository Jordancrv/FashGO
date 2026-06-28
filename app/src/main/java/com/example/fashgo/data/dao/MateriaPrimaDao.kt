package com.example.fashgo.data.dao

import androidx.room.*
import com.example.fashgo.data.entity.MateriaPrima
import kotlinx.coroutines.flow.Flow

@Dao
interface MateriaPrimaDao {
    @Query("SELECT * FROM materia_prima ORDER BY nombre ASC")
    fun getAll(): Flow<List<MateriaPrima>>

    @Query("SELECT COUNT(*) FROM materia_prima")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(materiaPrima: MateriaPrima)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materiasPrimas: List<MateriaPrima>)

    @Update
    suspend fun update(materiaPrima: MateriaPrima)

    @Delete
    suspend fun delete(materiaPrima: MateriaPrima)

    @Query("UPDATE materia_prima SET cantidad = cantidad - :uso WHERE id = :id AND cantidad >= :uso")
    suspend fun descontarMateriaPrima(id: Long, uso: Double): Int
}
