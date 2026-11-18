package com.example.fashgo.data.cloud

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.IOException

data class CloudUser(val nombre: String, val rol: String, val token: String, val id: String = "")
data class CloudLocation(val id: String, val codigo: String, val cantidad: Int)
data class CloudSale(val id: String, val fecha: String, val canal: String, val total: Double, val estado: String)
data class CloudSaleDetail(val nombre: String, val codigo: String, val ubicacion: String, val cantidad: Int, val precio: Double)
data class CloudProduct(
    val id: String, val nombre: String, val codigo: String, val precio: Double,
    val stockMinimo: Int, val locations: List<CloudLocation>
) {
    val stock: Int get() = locations.sumOf { it.cantidad }
}

object SupabaseClient {
    fun isConnectivityFailure(error:Throwable):Boolean = error is IOException || error.cause?.let(::isConnectivityFailure)==true
    private val session = MutableStateFlow<CloudUser?>(null)
    val currentUser: StateFlow<CloudUser?> = session
    private var refreshToken: String? = null
    private var expiresAt = 0L
    private val refreshLock = Mutex()
    private class HttpError(val status: Int, message: String) : Exception(message)
    fun clearSession() { session.value = null; refreshToken = null; expiresAt = 0L }
    suspend fun logout() {
        val token = session.value?.token
        clearSession()
        if (token != null) runCatching { withContext(Dispatchers.IO) { request("/auth/v1/logout", token, JSONObject()) } }
    }
    private suspend fun accessToken(): String = refreshLock.withLock {
        val user = session.value ?: throw IllegalStateException("Vuelve a iniciar sesión")
        if (System.currentTimeMillis() < expiresAt - 60000) return@withLock user.token
        val oldRefresh = refreshToken ?: throw IllegalStateException("Vuelve a iniciar sesión")
        val response = try { withContext(Dispatchers.IO) {
            JSONObject(request("/auth/v1/token?grant_type=refresh_token", body = JSONObject().put("refresh_token", oldRefresh)))
        } } catch (e: HttpError) {
            if (e.status == 400 || e.status == 401) clearSession()
            throw e
        }
        if (session.value?.id != user.id) throw IllegalStateException("La sesión ha cambiado")
        refreshToken = response.getString("refresh_token")
        expiresAt = System.currentTimeMillis() + response.getLong("expires_in") * 1000
        val token = response.getString("access_token")
        session.value = user.copy(token = token)
        token
    }
    suspend fun api(path: String, body: JSONObject? = null): String {
        val token = accessToken()
        return withContext(Dispatchers.IO) { request(path, token, body) }
    }
    suspend fun fetchAll(path: String): JSONArray {
        val all = JSONArray()
        var offset = 0
        while (true) {
            val page = JSONArray(api(path + (if (path.contains("?")) "&" else "?") + "limit=500&offset=$offset"))
            for (i in 0 until page.length()) all.put(page.get(i))
            if (page.length() < 500) break
            offset += page.length()
        }
        return all
    }

    private const val BASE_URL = "https://gqvasopdsnjgfirxjvfg.supabase.co"
    // La clave publicable identifica el proyecto; las políticas de Supabase protegen los datos.
    private const val PUBLIC_KEY = "sb_publishable_r8m6IvqP-yHHes_SiRyKUA_avJ_lPn8"

    private fun request(path: String, token: String? = null, body: JSONObject? = null): String {
        val connection = URL(BASE_URL + path).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 15000
            connection.readTimeout = 20000
            connection.setRequestProperty("apikey", PUBLIC_KEY)
            if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            if (status !in 200..299) {
                val raw = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val message = runCatching { JSONObject(raw).let { it.optString("message", it.optString("msg", it.optString("error_description"))) } }.getOrDefault("")
                val safe = when {
                    status == 401 -> "La sesión venció. Vuelve a iniciar sesión."
                    path.startsWith("/auth/") -> "No se pudo iniciar sesión. Comprueba correo, contraseña y confirmación de la cuenta."
                    status == 403 -> "Tu cuenta no tiene permiso para esta operación."
                    status == 404 -> "Falta instalar la actualización de la base de datos."
                    else -> message.ifBlank { "No se pudo completar la operación ($status)" }
                }
                throw HttpError(status, safe)
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun signIn(email: String, password: String): CloudUser = withContext(Dispatchers.IO) {
        val response = JSONObject(request("/auth/v1/token?grant_type=password", body = JSONObject().put("email", email.trim()).put("password", password)))
        val token = response.getString("access_token")
        val id = response.getJSONObject("user").getString("id")
        val users = JSONArray(request("/rest/v1/usuarios?select=nombre,rol&id=eq.$id&activo=eq.true", token))
        if (users.length() != 1) throw IllegalStateException("Esta cuenta no tiene un perfil activo en FashGO")
        val profile = users.getJSONObject(0)
        CloudUser(profile.getString("nombre"), profile.getString("rol"), token, id).also {
            session.value = it
            refreshToken = response.getString("refresh_token")
            expiresAt = System.currentTimeMillis() + response.getLong("expires_in") * 1000
        }
    }

    suspend fun createAuthAccount(email:String,password:String,metadata:JSONObject=JSONObject()):String = withContext(Dispatchers.IO) {
        val response = JSONObject(request("/auth/v1/signup", body = JSONObject()
            .put("email",email.trim()).put("password",password).put("data",metadata)))
        response.getJSONObject("user").getString("id")
    }

    suspend fun products(token: String): List<CloudProduct> = withContext(Dispatchers.IO) {
        val products = JSONArray(request("/rest/v1/productos?select=id,nombre,codigo,precio,stock_minimo&activo=eq.true&order=nombre.asc", token))
        val locations = JSONArray(request("/rest/v1/ubicaciones?select=id,codigo", token))
        val stocks = JSONArray(request("/rest/v1/existencias?select=producto_id,ubicacion_id,cantidad", token))
        val locationNames = (0 until locations.length()).associate { i -> locations.getJSONObject(i).let { it.getString("id") to it.getString("codigo") } }
        val byProduct = mutableMapOf<String, MutableList<CloudLocation>>()
        for (i in 0 until stocks.length()) {
            val item = stocks.getJSONObject(i)
            val name = locationNames[item.getString("ubicacion_id")] ?: throw IllegalStateException("Falta una ubicación del inventario")
            byProduct.getOrPut(item.getString("producto_id")) { mutableListOf() }.add(CloudLocation(item.getString("ubicacion_id"), name, item.getInt("cantidad")))
        }
        (0 until products.length()).map { i ->
            val item = products.getJSONObject(i)
            val id = item.getString("id")
            CloudProduct(id, item.getString("nombre"), item.getString("codigo"), item.getDouble("precio"), item.getInt("stock_minimo"), byProduct[id].orEmpty().sortedBy { it.codigo })
        }
    }

    suspend fun registerMovement(token: String, operation: String, product: String, location: String, quantity: Int, sale: Boolean) = withContext(Dispatchers.IO) {
        val body = JSONObject().put("p_operacion", operation).put("p_producto", product).put("p_ubicacion", location).put("p_cantidad", quantity)
        if (sale) body.put("p_canal", "Ventas Presenciales")
        request("/rest/v1/rpc/" + if (sale) "registrar_venta" else "registrar_ingreso", token, body)
    }

    suspend fun sales(token: String): List<CloudSale> = withContext(Dispatchers.IO) {
        val result = JSONArray(request("/rest/v1/ventas?select=id,fecha,canal,total,estado&order=fecha.desc&limit=100", token))
        (0 until result.length()).map { i ->
            val item = result.getJSONObject(i)
            CloudSale(item.getString("id"), item.getString("fecha"), item.getString("canal"), item.getDouble("total"), item.getString("estado"))
        }
    }

    suspend fun saleDetails(token: String, saleId: String): List<CloudSaleDetail> = withContext(Dispatchers.IO) {
        require(Regex("^[0-9a-fA-F-]{36}$").matches(saleId))
        val result = JSONArray(request("/rest/v1/detalle_venta?select=cantidad,precio_unitario,productos(nombre,codigo),ubicaciones(codigo)&venta_id=eq.$saleId", token))
        (0 until result.length()).map { i ->
            val item = result.getJSONObject(i)
            val product = item.getJSONObject("productos")
            val location = item.getJSONObject("ubicaciones")
            CloudSaleDetail(product.getString("nombre"), product.getString("codigo"), location.getString("codigo"), item.getInt("cantidad"), item.getDouble("precio_unitario"))
        }
    }
}
