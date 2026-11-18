package com.example.fashgo.ui.screens.audit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fashgo.data.cloud.SupabaseClient
import com.example.fashgo.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AuditItem(val id:String,val entidad:String,val accion:String,val detalle:String,val responsable:String,val fecha:Long)
data class AuditState(val loading:Boolean=true,val items:List<AuditItem> = emptyList(),val error:String?=null)
class AuditViewModel:ViewModel(){private val _state=MutableStateFlow(AuditState());val state=_state.asStateFlow();init{refresh()}
 fun refresh(){viewModelScope.launch{_state.value=_state.value.copy(loading=true,error=null);runCatching{val rows=SupabaseClient.fetchAll("/rest/v1/auditoria_movimientos?select=id,entidad,accion,detalle,fecha,usuarios(nombre,apellido,correo)&order=fecha.desc");(0 until rows.length()).map{i->rows.getJSONObject(i).let{r->val u=r.optJSONObject("usuarios");AuditItem(r.getString("id"),r.getString("entidad"),r.getString("accion"),r.getString("detalle"),u?.let{"${it.optString("nombre")} ${it.optString("apellido")}".trim().ifBlank{it.optString("correo")}}?:"Sistema",parseDate(r.getString("fecha")))}}}.onSuccess{_state.value=AuditState(false,it)}.onFailure{_state.value=AuditState(false,error=it.message)}}}
 private fun parseDate(v:String)=runCatching{SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX",Locale.ROOT).parse(v.replace(Regex("\\.\\d+"),""))?.time?:0}.getOrDefault(0)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun AuditScreen(onBack:()->Unit,vm:AuditViewModel=viewModel()) {val state by vm.state.collectAsState();val formatter=remember{SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault())};Scaffold(topBar={TopAppBar(title={Text("Movimientos",fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Filled.ArrowBack,"Volver")}},colors=TopAppBarDefaults.topAppBarColors(containerColor=Color.White))},containerColor=BackgroundWhite){padding->when{state.loading->Box(Modifier.fillMaxSize().padding(padding),contentAlignment=Alignment.Center){CircularProgressIndicator(color=MintGreen)};state.error!=null->Box(Modifier.fillMaxSize().padding(padding),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(state.error!!,color=ErrorRed);TextButton(onClick=vm::refresh){Text("Reintentar")}}};state.items.isEmpty()->Box(Modifier.fillMaxSize().padding(padding),contentAlignment=Alignment.Center){Text("Todavía no hay movimientos registrados",color=TextSecondary)};else->LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){items(state.items,key={it.id}){item->Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Row(Modifier.padding(15.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){Icon(Icons.Outlined.History,null,tint=PrimaryBlue);Column(Modifier.weight(1f)){Text(item.detalle,fontWeight=FontWeight.SemiBold,color=TextPrimary,fontSize=14.sp);Spacer(Modifier.height(5.dp));Text("${item.responsable} · ${formatter.format(Date(item.fecha))}",fontSize=12.sp,color=TextSecondary);Text("${item.entidad.replace('_',' ')} · ${item.accion}",fontSize=11.sp,color=TextTertiary)}}}}}}}}
