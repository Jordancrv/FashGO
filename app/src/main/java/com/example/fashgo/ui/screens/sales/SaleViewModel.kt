package com.example.fashgo.ui.screens.sales

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.remote.model.DetalleVenta
import com.example.fashgo.data.remote.model.Producto
import com.example.fashgo.data.remote.model.Venta
import com.example.fashgo.data.remote.RemoteProductRepository
import com.example.fashgo.data.remote.RemoteSalesRepository
import com.example.fashgo.data.cloud.SupabaseClient
import com.example.fashgo.data.offline.OfflineStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

data class CartItem(val producto: Producto, val cantidad: Int = 1, val precioUnitario: Double = producto.precio) { val subtotal: Double get() = cantidad * precioUnitario }
data class SaleUiState(val cart: List<CartItem> = emptyList(), val total: Double = 0.0, val montoRecibido: String = "", val cambio: Double = 0.0, val canal: String = "Ventas Presenciales", val cliente: String = "", val direccion: String = "", val error: String? = null, val saleCompleted: Boolean = false, val isProcessing: Boolean = false, val ventaDetalles: List<DetalleVenta> = emptyList(), val returnedByDetail:Map<String,Int> = emptyMap(), val pendingConfirmation: Boolean = false, val loading: Boolean = false)

class SaleViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SaleUiState())
    val uiState = _uiState.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private var operation: String? = null
    private var pendingBody: JSONObject? = null
    val isOwner get() = SupabaseClient.currentUser.value?.rol == "dueno"
    val allVentas = RemoteSalesRepository.sales
    val ventasDelDia = allVentas.map { list -> val start = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0) }.timeInMillis; list.filter { it.fecha >= start && it.estadoContable == "confirmada" } }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val searchResults = combine(RemoteProductRepository.products,_searchQuery) { list,query -> if(query.isBlank()) emptyList() else list.filter { p -> query.lowercase().split(' ').all { "${p.nombre} ${p.codigoProducto} ${p.tipo} ${p.color} ${p.talla}".lowercase().contains(it) } } }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val abcByProductId = combine(RemoteProductRepository.products,RemoteSalesRepository.sales,RemoteSalesRepository.details) { products,sales,details ->
        val ids = sales.filter { it.estadoContable == "confirmada" && it.fecha >= System.currentTimeMillis()-15L*86400000 }.map { it.id }.toSet()
        val counts = details.filter { it.ventaId in ids }.groupBy { it.productoId }.mapValues { it.value.sumOf { d -> d.cantidad } }
        val ordered = products.filter { (counts[it.id] ?: 0)>0 }.sortedByDescending { counts[it.id] }
        val top = ordered.take(kotlin.math.ceil(ordered.size/3.0).toInt()).map { it.id }.toSet()
        products.associate { it.id to if(it.id in top) "A" else if((counts[it.id]?:0)>0) "B" else "C" }
    }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyMap())
    init { refresh() }
    fun refresh() { viewModelScope.launch { _uiState.value = _uiState.value.copy(loading=true); try { RemoteProductRepository.refresh(); RemoteSalesRepository.refresh() } catch(e:CancellationException){throw e} catch(e:Exception){ _uiState.value=_uiState.value.copy(error=e.message ?: "No se pudieron cargar los datos") } finally { _uiState.value=_uiState.value.copy(loading=false) } } }
    private fun editable() = !_uiState.value.isProcessing && !_uiState.value.pendingConfirmation
    fun onSearchChange(query:String) { _searchQuery.value=query }
    fun addToCart(producto:Producto) { if(!editable()) return; val cart=_uiState.value.cart; val existing=cart.find { it.producto.id==producto.id }; val qty=(existing?.cantidad?:0)+1; if(qty>producto.stock){_uiState.value=_uiState.value.copy(error="Stock insuficiente");return}; updateCart(if(existing==null)cart+CartItem(producto) else cart.map { if(it.producto.id==producto.id)it.copy(cantidad=qty) else it });_searchQuery.value="" }
    fun addToCartByQrCode(code:String) { val p=RemoteProductRepository.products.value.find { it.qrCode==code || it.codigoProducto==code || it.id==code };if(p!=null)addToCart(p) else _uiState.value=_uiState.value.copy(error="Producto no encontrado") }
    fun updateQuantity(productId:String,newQuantity:Int) { if(!editable())return; val item=_uiState.value.cart.find { it.producto.id==productId }?:return; if(newQuantity>item.producto.stock){_uiState.value=_uiState.value.copy(error="Stock máximo: ${item.producto.stock}");return};updateCart(if(newQuantity<=0)_uiState.value.cart.filterNot { it.producto.id==productId } else _uiState.value.cart.map { if(it.producto.id==productId)it.copy(cantidad=newQuantity) else it }) }
    fun removeFromCart(productId:String) { if(editable())updateCart(_uiState.value.cart.filterNot { it.producto.id==productId }) }
    fun onMontoRecibidoChange(monto:String) { if(editable())_uiState.value=_uiState.value.copy(montoRecibido=monto,cambio=((monto.toDoubleOrNull()?:0.0)-_uiState.value.total).coerceAtLeast(0.0)) }
    fun onCanalChange(canal:String) { if(editable())_uiState.value=_uiState.value.copy(canal=canal) }
    fun onClienteChange(cliente:String) { if(editable())_uiState.value=_uiState.value.copy(cliente=cliente) }
    fun onDireccionChange(direccion:String) { if(editable())_uiState.value=_uiState.value.copy(direccion=direccion) }
    private fun updateCart(cart:List<CartItem>) { val total=cart.sumOf { it.subtotal };_uiState.value=_uiState.value.copy(cart=cart,total=total,cambio=((_uiState.value.montoRecibido.toDoubleOrNull()?:0.0)-total).coerceAtLeast(0.0),error=null) }
    fun completeSale(onSuccess:()->Unit) {
        if(_uiState.value.isProcessing)return
        val state=_uiState.value
        if(state.cart.isEmpty()){_uiState.value=state.copy(error="El carrito está vacío");return}
        val amount=state.montoRecibido.toDoubleOrNull()?:0.0
        if(!amount.isFinite() || (state.canal=="Ventas Presenciales" && amount<state.total)){_uiState.value=state.copy(error="Monto recibido insuficiente o inválido");return}
        if(pendingBody==null){ operation=UUID.randomUUID().toString(); pendingBody=JSONObject().put("p_operacion",operation).put("p_canal",state.canal).put("p_cliente",state.cliente.ifBlank { "Cliente General" }).put("p_direccion",state.direccion).put("p_monto_recibido",amount).put("p_lineas",JSONArray().apply { state.cart.forEach { put(JSONObject().put("producto_id",it.producto.id).put("cantidad",it.cantidad)) } }) }
        viewModelScope.launch { _uiState.value=_uiState.value.copy(isProcessing=true,pendingConfirmation=true,error=null);try {
            SupabaseClient.api("/rest/v1/rpc/registrar_venta_completa",pendingBody)
            pendingBody=null;operation=null;_uiState.value=SaleUiState(saleCompleted=true);onSuccess()
            runCatching { RemoteProductRepository.refresh();RemoteSalesRepository.refresh() }
        }catch(e:CancellationException){throw e}catch(e:Exception){if(SupabaseClient.isConnectivityFailure(e)){OfflineStore.enqueue("/rest/v1/rpc/registrar_venta_completa",pendingBody!!);pendingBody=null;operation=null;_uiState.value=SaleUiState(saleCompleted=true,error="Venta guardada sin conexión. Se sincronizará automáticamente.");onSuccess()}else _uiState.value=_uiState.value.copy(isProcessing=false,error="${e.message}. Reintenta sin cambiar la venta para confirmar el resultado.")}}
    }
    fun resetSale(){if(_uiState.value.pendingConfirmation || _uiState.value.isProcessing)return;_uiState.value=SaleUiState();_searchQuery.value=""}
    fun loadSaleDetails(ventaId:String){ viewModelScope.launch { try { RemoteSalesRepository.refresh();val returned=SupabaseClient.fetchAll("/rest/v1/detalle_devolucion?select=detalle_venta_id,cantidad,devoluciones!inner(venta_id)&devoluciones.venta_id=eq.$ventaId");val map=(0 until returned.length()).map{returned.getJSONObject(it)}.groupBy{it.getString("detalle_venta_id")}.mapValues{it.value.sumOf{r->r.getInt("cantidad")}};_uiState.value=_uiState.value.copy(ventaDetalles=RemoteSalesRepository.details.value.filter { it.ventaId==ventaId },returnedByDetail=map) }catch(e:CancellationException){throw e}catch(e:Exception){_uiState.value=_uiState.value.copy(error=e.message)} } }
    fun returnItem(ventaId:String,detailId:String,quantity:Int,reason:String,onSuccess:()->Unit){if(quantity<=0||reason.isBlank()){_uiState.value=_uiState.value.copy(error="Indica cantidad y motivo");return};viewModelScope.launch{try{_uiState.value=_uiState.value.copy(isProcessing=true,error=null);SupabaseClient.api("/rest/v1/rpc/registrar_devolucion",JSONObject().put("p_operacion",UUID.randomUUID().toString()).put("p_venta",ventaId).put("p_detalle",detailId).put("p_cantidad",quantity).put("p_motivo",reason));loadSaleDetails(ventaId);RemoteProductRepository.refresh();onSuccess()}catch(e:Exception){_uiState.value=_uiState.value.copy(error=e.message)}finally{_uiState.value=_uiState.value.copy(isProcessing=false)}}}
    fun cancelSale(id:String){viewModelScope.launch { _uiState.value=_uiState.value.copy(isProcessing=true);try { RemoteSalesRepository.cancel(id);loadSaleDetails(id) }catch(e:CancellationException){throw e}catch(e:Exception){_uiState.value=_uiState.value.copy(error=e.message)}finally{_uiState.value=_uiState.value.copy(isProcessing=false)} }}
    fun clearError(){_uiState.value=_uiState.value.copy(error=null)}
}

