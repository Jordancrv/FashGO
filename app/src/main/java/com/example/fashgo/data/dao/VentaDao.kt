package com.example.fashgo.data.dao

import androidx.room.*
import com.example.fashgo.data.entity.DetalleVenta
import com.example.fashgo.data.entity.ProductSalesStat
import com.example.fashgo.data.entity.Venta
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {
    @Insert
    suspend fun insertVenta(venta: Venta): Long

    @Insert
    suspend fun insertDetalles(detalles: List<DetalleVenta>)

    @Transaction
    suspend fun insertVentaConDetalles(venta: Venta, detalles: List<DetalleVenta>): Long {
        val ventaId = insertVenta(venta)
        val detallesConVentaId = detalles.map { it.copy(ventaId = ventaId) }
        insertDetalles(detallesConVentaId)
        return ventaId
    }

    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    fun getAll(): Flow<List<Venta>>

    @Query("SELECT * FROM ventas WHERE fecha BETWEEN :startDate AND :endDate ORDER BY fecha DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Venta>>

    @Query("SELECT * FROM ventas WHERE fecha >= :startOfDay AND fecha <= :endOfDay ORDER BY fecha DESC")
    fun getVentasDelDia(startOfDay: Long, endOfDay: Long): Flow<List<Venta>>

    @Query("SELECT SUM(total) FROM ventas WHERE fecha >= :startOfDay AND fecha <= :endOfDay")
    fun getTotalVentasDelDia(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM ventas WHERE fecha >= :startOfDay AND fecha <= :endOfDay")
    fun getCountVentasDelDia(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("SELECT * FROM detalle_ventas WHERE ventaId = :ventaId")
    suspend fun getDetallesByVentaId(ventaId: Long): List<DetalleVenta>

    @Query(
        """
        SELECT
            dv.productoId as productoId,
            SUM(dv.cantidad) as unidadesVendidas
        FROM detalle_ventas dv
        INNER JOIN ventas v ON v.id = dv.ventaId
        WHERE v.fecha BETWEEN :startDate AND :endDate
        GROUP BY dv.productoId
        """
    )
    fun getProductSalesStats(startDate: Long, endDate: Long): Flow<List<ProductSalesStat>>

    @Query("""
        SELECT
            0 as id,
            0 as ventaId,
            productoId,
            nombreProducto,
            MAX(codigoProducto) as codigoProducto,
            SUM(cantidad) as cantidad,
            AVG(precio) as precio
        FROM detalle_ventas 
        WHERE ventaId IN (SELECT id FROM ventas WHERE fecha BETWEEN :startDate AND :endDate)
        GROUP BY productoId 
        ORDER BY SUM(cantidad) DESC 
        LIMIT :limit
    """)
    suspend fun getTopProducts(startDate: Long, endDate: Long, limit: Int = 10): List<DetalleVenta>

    @Query("SELECT * FROM ventas WHERE canal = :canal ORDER BY fecha DESC")
    fun getByCanal(canal: String): Flow<List<Venta>>

    @Query("""
        SELECT canal, COUNT(*) as cantidadTransacciones, SUM(total) as totalVendido
        FROM ventas 
        WHERE fecha BETWEEN :startDate AND :endDate
        GROUP BY canal 
        ORDER BY SUM(total) DESC 
        LIMIT :limit
    """)
    suspend fun getTopChannels(startDate: Long, endDate: Long, limit: Int = 5): List<com.example.fashgo.data.entity.TopChannel>

    @Query("UPDATE ventas SET estado = :estado WHERE id = :id")
    suspend fun updateEstado(id: Long, estado: String)

    @Delete
    suspend fun deleteVenta(venta: Venta)
}
