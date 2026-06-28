package com.example.fashgo.data.repository

import com.example.fashgo.data.dao.MateriaPrimaDao
import com.example.fashgo.data.entity.MateriaPrima
import kotlinx.coroutines.flow.Flow

class MateriaPrimaRepository(private val dao: MateriaPrimaDao) {
    fun getAll(): Flow<List<MateriaPrima>> = dao.getAll()
    suspend fun getCount(): Int = dao.getCount()
    suspend fun insert(materiaPrima: MateriaPrima) = dao.insert(materiaPrima)
    suspend fun insertAll(materiasPrimas: List<MateriaPrima>) = dao.insertAll(materiasPrimas)
    suspend fun update(materiaPrima: MateriaPrima) = dao.update(materiaPrima)
    suspend fun delete(materiaPrima: MateriaPrima) = dao.delete(materiaPrima)
    suspend fun descontarMateriaPrima(id: Long, uso: Double): Int = dao.descontarMateriaPrima(id, uso)
}
