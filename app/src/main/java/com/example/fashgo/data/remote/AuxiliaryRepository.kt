package com.example.fashgo.data.remote

import com.example.fashgo.data.cloud.SupabaseClient
import com.example.fashgo.data.remote.model.MateriaPrima
import com.example.fashgo.data.remote.model.ProductoDerivado
import org.json.JSONObject

object AuxiliaryRepository {
    suspend fun materials(): List<MateriaPrima> {
        val rows = SupabaseClient.fetchAll("/rest/v1/materia_prima?select=*&activo=eq.true&order=id")
        return (0 until rows.length()).map { rows.getJSONObject(it).let { r ->
            MateriaPrima(r.getString("id"), r.getString("nombre"), r.optString("descripcion"), r.getDouble("cantidad"), r.getString("unidad_medida"), r.optString("proveedor"))
        } }
    }
    suspend fun saveMaterial(item: MateriaPrima) = SupabaseClient.api("/rest/v1/rpc/guardar_materia_prima", JSONObject().put("p_id", item.id).put("p_datos", JSONObject()
        .put("nombre", item.nombre).put("descripcion", item.descripcion).put("cantidad", item.cantidad).put("unidad_medida", item.unidadMedida).put("proveedor", item.proveedor)))
    suspend fun deleteMaterial(id: String) = SupabaseClient.api("/rest/v1/rpc/desactivar_materia_prima", JSONObject().put("p_id", id))
    suspend fun consumeMaterial(operation: String, id: String, quantity: Double) = SupabaseClient.api("/rest/v1/rpc/consumir_materia_prima", JSONObject().put("p_operacion", operation).put("p_id", id).put("p_cantidad", quantity))
    suspend fun derivatives(): List<ProductoDerivado> {
        val rows = SupabaseClient.fetchAll("/rest/v1/productos_derivados?select=*&activo=eq.true&order=id")
        return (0 until rows.length()).map { rows.getJSONObject(it).let { r ->
            ProductoDerivado(id = r.getString("id"), tipoPrenda = r.getString("tipo_prenda"), subcategoria = r.getString("subcategoria"), color = r.getString("color"), talla = r.getString("talla"), cantidad = r.getInt("cantidad"), precioUnitario = r.getDouble("precio_unitario"), rotacion = r.optString("rotacion", "Baja"), decisionSugerida = r.optString("decision_sugerida", "Derivado"), ubicacion = r.optString("ubicacion"), nivel = r.optString("nivel"), zonaAuxiliar = if(r.isNull("zona_auxiliar")) null else r.getInt("zona_auxiliar"), origen = r.optString("origen"), notas = r.optString("notas"))
        } }
    }
    suspend fun saveDerivative(item: ProductoDerivado) = SupabaseClient.api("/rest/v1/rpc/guardar_derivado", JSONObject().put("p_id", item.id).put("p_datos", JSONObject()
        .put("tipo_prenda", item.tipoPrenda).put("subcategoria", item.subcategoria).put("color", item.color).put("talla", item.talla).put("cantidad", item.cantidad).put("precio_unitario", item.precioUnitario).put("rotacion", item.rotacion).put("decision_sugerida", item.decisionSugerida).put("ubicacion", item.ubicacion).put("nivel", item.nivel).put("zona_auxiliar", item.zonaAuxiliar ?: JSONObject.NULL).put("origen", item.origen).put("notas", item.notas)))
    suspend fun deleteDerivative(id: String) = SupabaseClient.api("/rest/v1/rpc/desactivar_derivado", JSONObject().put("p_id", id))
}
