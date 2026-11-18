package com.example.fashgo.ui.screens.saleschannels
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.remote.RemoteProductRepository
import com.example.fashgo.data.remote.RemoteSalesRepository
import com.example.fashgo.data.remote.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
data class OrderWithDetails(val venta:Venta,val detalles:List<DetalleConProducto>)
data class DetalleConProducto(val detalle:DetalleVenta,val producto:Producto?)
class ChannelDetailViewModel(application:Application):AndroidViewModel(application){
 private val _orders=MutableStateFlow<List<OrderWithDetails>>(emptyList());val orders:StateFlow<List<OrderWithDetails>> = _orders;private var channel=""
 fun setCanal(canal:String){channel=canal;viewModelScope.launch{runCatching{RemoteProductRepository.refresh();RemoteSalesRepository.refresh()};rebuild()}}
 private fun rebuild(){_orders.value=RemoteSalesRepository.sales.value.filter{it.canal==channel&&it.estadoContable=="confirmada"}.map{sale->OrderWithDetails(sale,RemoteSalesRepository.details.value.filter{it.ventaId==sale.id}.map{DetalleConProducto(it,RemoteProductRepository.byId(it.productoId))})}}
 fun updateEstado(ventaId:String,nuevoEstado:String){viewModelScope.launch{runCatching{RemoteSalesRepository.updateShipping(ventaId,nuevoEstado)};rebuild()}}
}
