package com.example.fashgo.data.repository

import com.example.fashgo.data.dao.IngresoDao
import com.example.fashgo.data.entity.Ingreso
import kotlinx.coroutines.flow.Flow

class IngresoRepository(private val ingresoDao: IngresoDao) {
    fun getAll(): Flow<List<Ingreso>> = ingresoDao.getAll()
    fun getByProducto(productoId: Long): Flow<List<Ingreso>> = ingresoDao.getByProducto(productoId)
    suspend fun insert(ingreso: Ingreso): Long = ingresoDao.insert(ingreso)
}
