package com.example.fashgo.data.dao

import androidx.room.*
import com.example.fashgo.data.entity.Producto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos ORDER BY stock ASC, nombre ASC")
    fun getAll(): Flow<List<Producto>>

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun getById(id: Long): Producto?

    @Query("SELECT * FROM productos WHERE qrCode = :qrCode LIMIT 1")
    suspend fun getByQrCode(qrCode: String): Producto?

    @Query("SELECT * FROM productos WHERE codigoProducto = :codigoProducto LIMIT 1")
    suspend fun getByCodigoProducto(codigoProducto: String): Producto?

    @Query("SELECT codigoProducto FROM productos WHERE codigoProducto LIKE :prefix || '-%'")
    suspend fun getCodesByPrefix(prefix: String): List<String>

    @Query(
        """
        SELECT * FROM productos
        WHERE nombre LIKE '%' || :query || '%'
           OR codigoProducto LIKE '%' || :query || '%'
           OR tipo LIKE '%' || :query || '%'
           OR color LIKE '%' || :query || '%'
           OR talla LIKE '%' || :query || '%'
        ORDER BY stock ASC, nombre ASC
        """
    )
    fun search(query: String): Flow<List<Producto>>

    @Query("SELECT * FROM productos WHERE stock <= stockMinimo ORDER BY stock ASC")
    fun getLowStock(): Flow<List<Producto>>

    @Query("SELECT COUNT(*) FROM productos WHERE stock <= stockMinimo")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM productos")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun getCount(): Int

    @Query("SELECT COALESCE(SUM(stock), 0) FROM productos")
    fun getTotalUnits(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(producto: Producto): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<Producto>)

    @Update
    suspend fun update(producto: Producto)

    @Delete
    suspend fun delete(producto: Producto)

    @Query("UPDATE productos SET stock = stock - :cantidad WHERE id = :id AND stock >= :cantidad")
    suspend fun decrementStock(id: Long, cantidad: Int): Int

    @Query("UPDATE productos SET stock = stock + :cantidad WHERE id = :id")
    suspend fun incrementStock(id: Long, cantidad: Int)
}
