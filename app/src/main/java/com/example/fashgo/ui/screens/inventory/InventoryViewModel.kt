package com.example.fashgo.ui.screens.inventory
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.cloud.SupabaseClient
import com.example.fashgo.data.offline.OfflineStore
import com.example.fashgo.data.remote.RemoteProductRepository
import com.example.fashgo.data.remote.RemoteSalesRepository
import com.example.fashgo.data.remote.model.Ingreso
import com.example.fashgo.data.remote.model.Producto
import com.example.fashgo.util.InventorySummary
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID
data class InventoryUiState(val error:String?=null,val success:String?=null,val isLoading:Boolean=false)
class InventoryViewModel(application:Application):AndroidViewModel(application){
 private val _ui=MutableStateFlow(InventoryUiState());val uiState=_ui.asStateFlow();private val _query=MutableStateFlow("");val searchQuery=_query.asStateFlow();private val _ingresos=MutableStateFlow<List<Ingreso>>(emptyList());val ingresos=_ingresos.asStateFlow()
 val products=combine(RemoteProductRepository.products,_query){list,q->if(q.isBlank())list else list.filter{"${it.nombre} ${it.codigoProducto} ${it.tipo} ${it.color} ${it.talla}".contains(q,true)}}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val summary=products.map{InventorySummary(it.size,it.sumOf{p->p.stock},it.sumOf{p->p.stock*p.precio},it.count{p->p.semaforoEstado=="ROJO"},it.count{p->p.semaforoEstado=="AMARILLO"},it.count{p->p.semaforoEstado=="VERDE"})}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),InventorySummary())
 val abcByProductId=combine(RemoteProductRepository.products,RemoteSalesRepository.sales,RemoteSalesRepository.details){all,sales,details->val valid=sales.filter{it.estadoContable=="confirmada"&&it.fecha>=System.currentTimeMillis()-15L*86_400_000}.map{it.id}.toSet();val sold=details.filter{it.ventaId in valid}.groupBy{it.productoId}.mapValues{it.value.sumOf{d->d.cantidad}};val ordered=all.filter{(sold[it.id]?:0)>0}.sortedByDescending{sold[it.id]?:0};val top=ordered.take(kotlin.math.ceil(ordered.size/3.0).toInt()).map{it.id}.toSet();all.associate{it.id to if(it.id in top)"A" else if((sold[it.id]?:0)>0)"B" else "C"}}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyMap())
 init{refresh()};private fun refresh()=viewModelScope.launch{try{RemoteProductRepository.refresh();RemoteSalesRepository.refresh();val rows=SupabaseClient.fetchAll("/rest/v1/movimientos_inventario?select=id,producto_id,cantidad,fecha,productos(nombre,codigo)&tipo=eq.entrada&order=fecha.desc");_ingresos.value=(0 until rows.length()).map{i->rows.getJSONObject(i).let{r->val p=r.getJSONObject("productos");Ingreso(r.getString("id"),r.getString("producto_id"),p.getString("nombre"),p.getString("codigo"),r.getInt("cantidad"),java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(r.getString("fecha").replace(Regex("\\.\\d+"),""))?.time?:0)}}}catch(e:Exception){_ui.value=_ui.value.copy(error=e.message)}}
 fun onSearchChange(v:String){_query.value=v}
 fun registrarIngreso(productoId:String,nombreProducto:String,codigoProducto:String,cantidad:String,onSuccess:()->Unit){val q=cantidad.toIntOrNull();val p=RemoteProductRepository.byId(productoId);val location=p?.existencias?.firstOrNull();if(q==null||q<=0||location==null){_ui.value=_ui.value.copy(error="Cantidad o ubicación inválida");return};viewModelScope.launch{val body=JSONObject().put("p_operacion",UUID.randomUUID().toString()).put("p_producto",productoId).put("p_ubicacion",location.ubicacionId).put("p_cantidad",q);try{_ui.value=_ui.value.copy(isLoading=true);SupabaseClient.api("/rest/v1/rpc/registrar_ingreso",body);refresh();_ui.value=_ui.value.copy(success="Ingreso registrado");onSuccess()}catch(e:Exception){if(SupabaseClient.isConnectivityFailure(e)){OfflineStore.enqueue("/rest/v1/rpc/registrar_ingreso",body);_ui.value=_ui.value.copy(success="Ingreso guardado sin conexión; se sincronizará automáticamente");onSuccess()}else _ui.value=_ui.value.copy(error=e.message)}finally{_ui.value=_ui.value.copy(isLoading=false)}}}
 fun clearMessages(){_ui.value=InventoryUiState()};fun getProductById(id:String,onResult:(Producto?)->Unit){onResult(RemoteProductRepository.byId(id))};fun getProductByQrCode(code:String,onResult:(Producto?)->Unit){onResult(RemoteProductRepository.byCode(code))}
}
