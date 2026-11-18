package com.example.fashgo.ui.screens.home
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.remote.RemoteProductRepository
import com.example.fashgo.data.remote.RemoteSalesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
class HomeViewModel(application:Application):AndroidViewModel(application){
 private val start=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}.timeInMillis
 val totalVentasDelDia=RemoteSalesRepository.sales.map{it.filter{v->v.fecha>=start&&v.estadoContable=="confirmada"}.sumOf{v->v.total}}
 val countVentasDelDia=RemoteSalesRepository.sales.map{it.count{v->v.fecha>=start&&v.estadoContable=="confirmada"}}
 val lowStockCount=combine(RemoteProductRepository.products,RemoteSalesRepository.sales,RemoteSalesRepository.details){products,sales,details->val ids=sales.filter{it.estadoContable=="confirmada"&&it.fecha>=System.currentTimeMillis()-30L*86_400_000}.map{it.id}.toSet();val moved=details.filter{it.ventaId in ids}.map{it.productoId}.toSet();products.count{it.stock<=it.stockMinimo||it.id !in moved}}
 val totalProducts=RemoteProductRepository.products.map{it.size};val totalUnits=RemoteProductRepository.products.map{it.sumOf{p->p.stock}}
 init{viewModelScope.launch{runCatching{RemoteProductRepository.refresh();RemoteSalesRepository.refresh()}}}
 fun resolveProductIdByQrCode(code:String,onResult:(String?)->Unit){onResult(RemoteProductRepository.byCode(code)?.id)}
}
