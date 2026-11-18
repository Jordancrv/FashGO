package com.example.fashgo.ui.screens.reports
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.remote.RemoteProductRepository
import com.example.fashgo.data.remote.RemoteSalesRepository
import com.example.fashgo.data.remote.model.DetalleVenta
import com.example.fashgo.data.remote.model.Venta
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.ceil
data class AbcSalesRow(val productId:String,val nombreProducto:String,val codigoProducto:String,val unidadesVendidas:Int,val clasificacion:String)
data class TopChannel(val canal:String,val cantidadTransacciones:Int,val totalVendido:Double)
data class ReportsUiState(val ventasHoy:List<Venta> = emptyList(),val totalHoy:Double=0.0,val countHoy:Int=0,val topProducts:List<DetalleVenta> = emptyList(),val topChannels:List<TopChannel> = emptyList(),val ventasRango:List<Venta> = emptyList(),val totalRango:Double=0.0)
class ReportsViewModel(application:Application):AndroidViewModel(application){
 private val _uiState=MutableStateFlow(ReportsUiState());val uiState:StateFlow<ReportsUiState> = _uiState
 val abcSalesRows=combine(RemoteProductRepository.products,RemoteSalesRepository.sales,RemoteSalesRepository.details){products,sales,details->
  val valid=sales.filter{it.estadoContable=="confirmada"&&it.fecha>=System.currentTimeMillis()-15L*86_400_000}.map{it.id}.toSet()
  val sold=details.filter{it.ventaId in valid}.groupBy{it.productoId}.mapValues{it.value.sumOf{d->d.cantidad}}
  val ordered=products.filter{(sold[it.id]?:0)>0}.sortedByDescending{sold[it.id]?:0};val a=ordered.take(ceil(ordered.size/3.0).toInt()).map{it.id}.toSet()
  if(ordered.isEmpty()) emptyList() else products.map{p->AbcSalesRow(p.id,p.nombre,p.codigoProducto,sold[p.id]?:0,if(p.id in a)"A" else if((sold[p.id]?:0)>0)"B" else "C")}.sortedWith(compareBy<AbcSalesRow>{when(it.clasificacion){"A"->0;"B"->1;else->2}}.thenByDescending{it.unidadesVendidas})
 }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 init{viewModelScope.launch{runCatching{RemoteProductRepository.refresh();RemoteSalesRepository.refresh()};recalculate()}}
 private fun startOfDay()=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}.timeInMillis
 private fun recalculate(){val sales=RemoteSalesRepository.sales.value.filter{it.estadoContable=="confirmada"};val details=RemoteSalesRepository.details.value;val today=sales.filter{it.fecha>=startOfDay()};val recent=sales.filter{it.fecha>=System.currentTimeMillis()-30L*86_400_000};val ids=recent.map{it.id}.toSet();val top=details.filter{it.ventaId in ids}.groupBy{it.productoId}.values.map{rows->rows.first().copy(cantidad=rows.sumOf{it.cantidad})}.sortedByDescending{it.cantidad}.take(5);val channels=recent.groupBy{it.canal}.map{(c,rows)->TopChannel(c,rows.size,rows.sumOf{it.total})}.sortedByDescending{it.totalVendido}.take(5);_uiState.value=ReportsUiState(today,today.sumOf{it.total},today.size,top,channels)}
 fun loadRangeReport(startDate:Long,endDate:Long){val rows=RemoteSalesRepository.sales.value.filter{it.estadoContable=="confirmada"&&it.fecha in startDate..endDate};_uiState.value=_uiState.value.copy(ventasRango=rows,totalRango=rows.sumOf{it.total})}
}
