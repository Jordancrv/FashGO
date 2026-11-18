package com.example.fashgo.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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

data class ProfileData(val nombre:String="",val apellido:String="",val ruc:String="",val correo:String="",val sexo:String="",val fechaNacimiento:String="",val rol:String="")
data class ProfileState(val loading:Boolean=true,val profile:ProfileData?=null,val error:String?=null)
class ProfileViewModel:ViewModel(){
 private val _state=MutableStateFlow(ProfileState());val state=_state.asStateFlow()
 init{refresh()}
 fun refresh(){viewModelScope.launch{_state.value=ProfileState();runCatching{val id=SupabaseClient.currentUser.value?.id?:error("Sesión no disponible");val rows=SupabaseClient.fetchAll("/rest/v1/usuarios?select=nombre,apellido,ruc,correo,sexo,fecha_nacimiento,rol&id=eq.$id");if(rows.length()!=1)error("No se encontró el perfil");rows.getJSONObject(0)}.onSuccess{r->_state.value=ProfileState(false,ProfileData(r.optString("nombre"),r.optString("apellido"),r.optString("ruc"),r.optString("correo"),r.optString("sexo").replace('_',' ').replaceFirstChar{it.uppercase()},r.optString("fecha_nacimiento"),r.optString("rol").replaceFirstChar{it.uppercase()}))}.onFailure{_state.value=ProfileState(false,error=it.message)}}}
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ProfileScreen(onBack:()->Unit,vm:ProfileViewModel=viewModel()){
 val state by vm.state.collectAsState()
 Scaffold(topBar={TopAppBar(title={Text("Mi perfil",fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Filled.ArrowBack,"Volver")}},colors=TopAppBarDefaults.topAppBarColors(containerColor=Color.White))},containerColor=BackgroundWhite){padding->
  Box(Modifier.fillMaxSize().padding(padding).padding(20.dp),contentAlignment=Alignment.TopCenter){when{
   state.loading->CircularProgressIndicator(color=MintGreen)
   state.error!=null->Column(horizontalAlignment=Alignment.CenterHorizontally){Text(state.error!!,color=ErrorRed);TextButton(onClick=vm::refresh){Text("Reintentar")}}
   else->state.profile?.let{p->Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)){
    Surface(shape=CircleShape,color=MintGreen.copy(alpha=.15f),modifier=Modifier.size(92.dp)){Box(contentAlignment=Alignment.Center){Icon(Icons.Filled.Person,null,tint=MintGreen,modifier=Modifier.size(46.dp))}}
    Text("${p.nombre} ${p.apellido}".trim(),fontSize=24.sp,fontWeight=FontWeight.Bold,color=TextPrimary)
    Surface(shape=RoundedCornerShape(20.dp),color=LightMint){Text(p.rol,modifier=Modifier.padding(horizontal=14.dp,vertical=6.dp),color=MintGreenDark,fontWeight=FontWeight.SemiBold)}
    Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){ProfileRow("Correo",p.correo);ProfileRow("RUT",p.ruc);ProfileRow("Sexo",p.sexo);ProfileRow("Fecha de nacimiento",p.fechaNacimiento)}}
   }}
  }}
 }
}
@Composable private fun ProfileRow(label:String,value:String){Column{Text(label,fontSize=12.sp,color=TextSecondary);Text(value.ifBlank{"Sin información"},fontSize=15.sp,fontWeight=FontWeight.Medium,color=TextPrimary);HorizontalDivider(Modifier.padding(top=12.dp),color=Color(0xFFF0F2F5))}}
