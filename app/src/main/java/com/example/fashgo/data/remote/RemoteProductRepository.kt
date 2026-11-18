package com.example.fashgo.data.remote

import com.example.fashgo.data.cloud.SupabaseClient
import com.example.fashgo.data.remote.model.Existencia
import com.example.fashgo.data.remote.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import com.example.fashgo.data.offline.OfflineStore

object RemoteProductRepository {
    private val _products = MutableStateFlow<List<Producto>>(emptyList())
    val products = _products.asStateFlow()
    fun clear() { _products.value = emptyList() }

    suspend fun refresh() {
        try {
        val productRows = SupabaseClient.fetchAll("/rest/v1/productos?select=*&activo=eq.true&order=nombre.asc,id.asc")
        val locationRows = SupabaseClient.fetchAll("/rest/v1/ubicaciones?select=id,codigo&order=codigo.asc")
        val stockRows = SupabaseClient.fetchAll("/rest/v1/existencias?select=producto_id,ubicacion_id,cantidad")
        val locations = (0 until locationRows.length()).associate { i -> locationRows.getJSONObject(i).let { it.getString("id") to it.getString("codigo") } }
        val stocks = mutableMapOf<String, MutableList<Existencia>>()
        for (i in 0 until stockRows.length()) stockRows.getJSONObject(i).let { row ->
            val locationId = row.getString("ubicacion_id")
            stocks.getOrPut(row.getString("producto_id")) { mutableListOf() }.add(Existencia(locationId, locations[locationId] ?: "Sin ubicación", row.getInt("cantidad")))
        }
        _products.value = (0 until productRows.length()).map { i -> productRows.getJSONObject(i).let { r ->
            val id = r.getString("id"); val inventory = stocks[id].orEmpty().sortedBy { it.codigo }; val stock = inventory.sumOf { it.cantidad }
            Producto(id, r.getString("nombre"), r.optString("parte_general"), r.optString("tipo"), r.optString("diseno"), r.optString("tipo_manga"), r.optString("tipo_cuello"), r.optString("color"), r.optString("talla"), r.getDouble("precio"), stock, r.optInt("stock_referencia", stock), inventory.joinToString(" + ") { it.codigo }, r.getString("codigo"), r.getString("codigo"), r.optInt("stock_minimo", 0), "C", inventory)
        } }
        OfflineStore.cacheProducts(_products.value)
        } catch(e:Exception) {
            val cached=OfflineStore.cachedProducts()
            if(cached.isEmpty()) throw e
            _products.value=cached
        }
    }
    fun loadCached(){val cached=OfflineStore.cachedProducts();if(cached.isNotEmpty())_products.value=cached}
    fun byId(id: String) = products.value.find { it.id == id }
    fun byCode(code: String) = products.value.find { it.codigoProducto == code || it.qrCode == code }
    suspend fun save(product: Producto, stockInitial: Int, location: String) = SupabaseClient.api("/rest/v1/rpc/guardar_producto", JSONObject().put("p_id", product.id).put("p_datos", JSONObject()
        .put("codigo", product.codigoProducto).put("nombre", product.nombre).put("parte_general", product.parteGeneral).put("tipo", product.tipo).put("diseno", product.diseno).put("tipo_manga", product.tipoManga).put("tipo_cuello", product.tipoCuello).put("color", product.color).put("talla", product.talla).put("precio", product.precio).put("stock_minimo", product.stockMinimo).put("stock_inicial", stockInitial).put("ubicacion", location)))
    suspend fun deactivate(id: String) = SupabaseClient.api("/rest/v1/rpc/desactivar_producto", JSONObject().put("p_id", id))
}
