package com.example.fashgo.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.database.FashGoDatabase
import com.example.fashgo.data.repository.ProductoRepository
import com.example.fashgo.data.repository.VentaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FashGoDatabase.getDatabase(application)
    private val productoRepository = ProductoRepository(db.productoDao())
    private val ventaRepository = VentaRepository(db.ventaDao())

    private fun getStartOfDay(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    val totalVentasDelDia: Flow<Double?> = ventaRepository.getTotalVentasDelDia(getStartOfDay(), getEndOfDay())
    val countVentasDelDia: Flow<Int> = ventaRepository.getCountVentasDelDia(getStartOfDay(), getEndOfDay())
    val lowStockCount: Flow<Int> = productoRepository.getLowStockCount()
    val totalProducts: Flow<Int> = productoRepository.getTotalCount()
    val totalUnits: Flow<Int> = productoRepository.getTotalUnits()

    fun resolveProductIdByQrCode(qrCode: String, onResult: (Long?) -> Unit) {
        viewModelScope.launch {
            val product = productoRepository.getByQrCode(qrCode)
                ?: qrCode.substringAfter("FASHGO-", "").toLongOrNull()?.let { productoRepository.getById(it) }
            onResult(product?.id)
        }
    }
}
