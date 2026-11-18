package com.example.fashgo.data.offline
import android.content.Context
import com.example.fashgo.data.cloud.SupabaseClient
import com.example.fashgo.data.remote.model.Existencia
import com.example.fashgo.data.remote.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object OfflineStore{
 private lateinit var context:Context
 private val _pending=MutableStateFlow(0);val pending=_pending.asStateFlow()
 fun init(value:Context){context=value.applicationContext;_pending.value=queue().length()}
 private fun prefs()=context.getSharedPreferences("fashgo_offline",Context.MODE_PRIVATE)
 private fun queue()=runCatching{JSONArray(prefs().getString("queue","[]"))}.getOrElse{JSONArray()}
 @Synchronized fun enqueue(path:String,body:JSONObject){val q=queue();q.put(JSONObject().put("id",UUID.randomUUID().toString()).put("path",path).put("body",body));prefs().edit().putString("queue",q.toString()).apply();_pending.value=q.length()}
 suspend fun sync():Int{val q=queue();val remaining=JSONArray();var sent=0;for(i in 0 until q.length()){val op=q.getJSONObject(i);try{SupabaseClient.api(op.getString("path"),op.getJSONObject("body"));sent++}catch(e:Exception){remaining.put(op)}};prefs().edit().putString("queue",remaining.toString()).apply();_pending.value=remaining.length();return sent}
 fun cacheProducts(products:List<Producto>){val a=JSONArray();products.forEach{p->a.put(JSONObject().put("id",p.id).put("nombre",p.nombre).put("parteGeneral",p.parteGeneral).put("tipo",p.tipo).put("diseno",p.diseno).put("tipoManga",p.tipoManga).put("tipoCuello",p.tipoCuello).put("color",p.color).put("talla",p.talla).put("precio",p.precio).put("stock",p.stock).put("stockReferencia",p.stockReferencia).put("ubicacion",p.ubicacion).put("codigo",p.codigoProducto).put("stockMinimo",p.stockMinimo).put("existencias",JSONArray().apply{p.existencias.forEach{e->put(JSONObject().put("id",e.ubicacionId).put("codigo",e.codigo).put("cantidad",e.cantidad))}}))};prefs().edit().putString("products",a.toString()).apply()}
 fun cachedProducts():List<Producto>{val a=runCatching{JSONArray(prefs().getString("products","[]"))}.getOrElse{JSONArray()};return(0 until a.length()).map{i->a.getJSONObject(i).let{p->val ex=p.getJSONArray("existencias");Producto(p.getString("id"),p.getString("nombre"),p.optString("parteGeneral"),p.optString("tipo"),p.optString("diseno"),p.optString("tipoManga"),p.optString("tipoCuello"),p.optString("color"),p.optString("talla"),p.getDouble("precio"),p.getInt("stock"),p.getInt("stockReferencia"),p.optString("ubicacion"),p.getString("codigo"),p.getString("codigo"),p.getInt("stockMinimo"),"C",(0 until ex.length()).map{j->ex.getJSONObject(j).let{e->Existencia(e.getString("id"),e.getString("codigo"),e.getInt("cantidad"))}})}}}
}
