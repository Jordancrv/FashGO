package com.example.fashgo.data.repository

import com.example.fashgo.data.dao.VentaDao
import com.example.fashgo.data.entity.DetalleVenta
import com.example.fashgo.data.entity.ProductSalesStat
import com.example.fashgo.data.entity.Venta
import kotlinx.coroutines.flow.Flow

class VentaRepository(private val ventaDao: VentaDao) {
    fun getAll(): Flow<List<Venta>> = ventaDao.getAll()

    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Venta>> =
        ventaDao.getByDateRange(startDate, endDate)

    fun getVentasDelDia(startOfDay: Long, endOfDay: Long): Flow<List<Venta>> =
        ventaDao.getVentasDelDia(startOfDay, endOfDay)

    fun getTotalVentasDelDia(startOfDay: Long, endOfDay: Long): Flow<Double?> =
        ventaDao.getTotalVentasDelDia(startOfDay, endOfDay)

    fun getCountVentasDelDia(startOfDay: Long, endOfDay: Long): Flow<Int> =
        ventaDao.getCountVentasDelDia(startOfDay, endOfDay)

    fun getProductSalesStats(startDate: Long, endDate: Long): Flow<List<ProductSalesStat>> =
        ventaDao.getProductSalesStats(startDate, endDate)

    suspend fun insertVentaConDetalles(venta: Venta, detalles: List<DetalleVenta>): Long =
        ventaDao.insertVentaConDetalles(venta, detalles)

    suspend fun getDetallesByVentaId(ventaId: Long): List<DetalleVenta> =
        ventaDao.getDetallesByVentaId(ventaId)

    suspend fun getTopProducts(startDate: Long, endDate: Long, limit: Int = 10): List<DetalleVenta> =
        ventaDao.getTopProducts(startDate, endDate, limit)

    suspend fun getTopChannels(startDate: Long, endDate: Long, limit: Int = 5): List<com.example.fashgo.data.entity.TopChannel> =
        ventaDao.getTopChannels(startDate, endDate, limit)

    suspend fun deleteVenta(venta: Venta) = ventaDao.deleteVenta(venta)
}
