package com.example.fashgo.ui.screens.saleschannels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.database.FashGoDatabase
import com.example.fashgo.data.entity.DetalleVenta
import com.example.fashgo.data.entity.Producto
import com.example.fashgo.data.entity.Venta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class OrderWithDetails(
    val venta: Venta,
    val detalles: List<DetalleConProducto>
)

data class DetalleConProducto(
    val detalle: DetalleVenta,
    val producto: Producto?
)

class ChannelDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FashGoDatabase.getDatabase(application)
    private val ventaDao = db.ventaDao()
    private val productoDao = db.productoDao()

    private val _orders = MutableStateFlow<List<OrderWithDetails>>(emptyList())
    val orders: StateFlow<List<OrderWithDetails>> = _orders

    fun setCanal(canal: String) {
        viewModelScope.launch {
            ventaDao.getByCanal(canal).collectLatest { ventas ->
                val ordersList = ventas.map { venta ->
                    val detalles = ventaDao.getDetallesByVentaId(venta.id)
                    val detallesConProducto = detalles.map { det ->
                        val prod = productoDao.getById(det.productoId)
                        DetalleConProducto(det, prod)
                    }
                    OrderWithDetails(venta, detallesConProducto)
                }
                _orders.value = ordersList
            }
        }
    }

    fun updateEstado(ventaId: Long, nuevoEstado: String) {
        viewModelScope.launch {
            ventaDao.updateEstado(ventaId, nuevoEstado)
        }
    }
}
