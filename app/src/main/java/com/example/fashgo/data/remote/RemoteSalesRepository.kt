package com.example.fashgo.data.remote

import com.example.fashgo.data.cloud.SupabaseClient
import com.example.fashgo.data.remote.model.Venta
import com.example.fashgo.data.remote.model.DetalleVenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object RemoteSalesRepository {
    private val _sales = MutableStateFlow<List<Venta>>(emptyList())
    val sales = _sales.asStateFlow()
    private val _details = MutableStateFlow<List<DetalleVenta>>(emptyList())
    val details = _details.asStateFlow()
    fun clear() { _sales.value = emptyList(); _details.value = emptyList() }
    private fun timestamp(value: String): Long {
        val normalized = value.replace(Regex("\\.\\d+"), "")
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ROOT).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(normalized)?.time ?: error("Fecha de venta inválida")
    }
    suspend fun refresh() {
        val rows = SupabaseClient.fetchAll("/rest/v1/ventas?select=*&order=fecha.desc,id.asc")
        val detailRows = SupabaseClient.fetchAll("/rest/v1/detalle_venta?select=*,productos(nombre,codigo),ubicaciones(codigo)&order=id.asc")
        val newSales = (0 until rows.length()).map { i -> rows.getJSONObject(i).let { r ->
            Venta(r.getString("id"),timestamp(r.getString("fecha")),r.getDouble("total"),r.getString("canal"),r.optString("estado_envio","Enviado"),r.optString("cliente","Cliente General"),r.optString("direccion",""),r.optString("codigo_orden",""),r.getString("estado"))
        } }
        val newDetails = (0 until detailRows.length()).map { i -> detailRows.getJSONObject(i).let { r ->
            val product = r.optJSONObject("productos")
            DetalleVenta(r.getString("id"),r.getString("venta_id"),r.getString("producto_id"),r.optString("nombre_producto").takeIf { it.isNotBlank() && it != "null" } ?: product?.optString("nombre").orEmpty(),r.optString("codigo_producto").takeIf { it.isNotBlank() && it != "null" } ?: product?.optString("codigo").orEmpty(),r.getInt("cantidad"),r.getDouble("precio_unitario"),r.optJSONObject("ubicaciones")?.optString("codigo").orEmpty())
        } }
        _details.value = newDetails
        _sales.value = newSales
    }
    suspend fun updateShipping(id:String, state:String) {
        SupabaseClient.api("/rest/v1/rpc/cambiar_estado_envio",JSONObject().put("p_venta",id).put("p_estado",state)); refresh()
    }
    suspend fun cancel(id:String) {
        SupabaseClient.api("/rest/v1/rpc/anular_venta",JSONObject().put("p_venta",id)); refresh(); RemoteProductRepository.refresh()
    }
}
