package com.example.fashgo.data.repository

import com.example.fashgo.data.dao.ProductoDao
import com.example.fashgo.data.entity.Producto
import kotlinx.coroutines.flow.Flow

class ProductoRepository(private val productoDao: ProductoDao) {
    fun getAll(): Flow<List<Producto>> = productoDao.getAll()
    fun search(query: String): Flow<List<Producto>> = productoDao.search(query)
    fun getLowStock(): Flow<List<Producto>> = productoDao.getLowStock()
    fun getLowStockCount(): Flow<Int> = productoDao.getLowStockCount()
    fun getTotalCount(): Flow<Int> = productoDao.getTotalCount()
    fun getTotalUnits(): Flow<Int> = productoDao.getTotalUnits()

    suspend fun getById(id: Long): Producto? = productoDao.getById(id)
    suspend fun getByQrCode(qrCode: String): Producto? = productoDao.getByQrCode(qrCode)
    suspend fun getByCodigoProducto(codigoProducto: String): Producto? = productoDao.getByCodigoProducto(codigoProducto)
    suspend fun getCodesByPrefix(prefix: String): List<String> = productoDao.getCodesByPrefix(prefix)
    suspend fun getCount(): Int = productoDao.getCount()
    suspend fun insert(producto: Producto): Long = productoDao.insert(producto)
    suspend fun insertAll(productos: List<Producto>) = productoDao.insertAll(productos)
    suspend fun update(producto: Producto) = productoDao.update(producto)
    suspend fun delete(producto: Producto) = productoDao.delete(producto)
    suspend fun decrementStock(id: Long, cantidad: Int): Int = productoDao.decrementStock(id, cantidad)
    suspend fun incrementStock(id: Long, cantidad: Int) = productoDao.incrementStock(id, cantidad)
}
