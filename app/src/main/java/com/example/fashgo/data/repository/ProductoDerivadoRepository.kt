package com.example.fashgo.data.repository

import com.example.fashgo.data.dao.ProductoDerivadoDao
import com.example.fashgo.data.entity.ProductoDerivado
import kotlinx.coroutines.flow.Flow

class ProductoDerivadoRepository(private val dao: ProductoDerivadoDao) {
    fun getAll(): Flow<List<ProductoDerivado>> = dao.getAll()
    suspend fun getCount(): Int = dao.getCount()
    suspend fun insert(productoDerivado: ProductoDerivado): Long = dao.insert(productoDerivado)
    suspend fun insertAll(productos: List<ProductoDerivado>) = dao.insertAll(productos)
    suspend fun update(productoDerivado: ProductoDerivado) = dao.update(productoDerivado)
    suspend fun delete(productoDerivado: ProductoDerivado) = dao.delete(productoDerivado)
}
