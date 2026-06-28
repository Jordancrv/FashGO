package com.example.fashgo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fashgo.data.entity.ProductoDerivado
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDerivadoDao {
    @Query("SELECT * FROM productos_derivados ORDER BY fechaRegistro DESC, tipoPrenda ASC")
    fun getAll(): Flow<List<ProductoDerivado>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(productoDerivado: ProductoDerivado): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<ProductoDerivado>)

    @Update
    suspend fun update(productoDerivado: ProductoDerivado)

    @Delete
    suspend fun delete(productoDerivado: ProductoDerivado)

    @Query("SELECT COUNT(*) FROM productos_derivados")
    suspend fun getCount(): Int
}
