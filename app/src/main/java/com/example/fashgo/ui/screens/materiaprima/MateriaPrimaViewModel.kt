package com.example.fashgo.ui.screens.materiaprima
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.remote.AuxiliaryRepository
import com.example.fashgo.data.remote.model.MateriaPrima
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
data class MateriaPrimaUiState(val selectedMateriaPrima:MateriaPrima?=null,val isLoading:Boolean=false,val error:String?=null)
class MateriaPrimaViewModel(application:Application):AndroidViewModel(application){
 private val _list=MutableStateFlow<List<MateriaPrima>>(emptyList());val materiaPrimaList=_list.asStateFlow();private val _ui=MutableStateFlow(MateriaPrimaUiState());val uiState=_ui.asStateFlow()
 init{refresh()};private fun refresh()=viewModelScope.launch{try{_list.value=AuxiliaryRepository.materials()}catch(e:Exception){_ui.value=_ui.value.copy(error=e.message)}}
 fun selectMateriaPrima(v:MateriaPrima?){_ui.value=_ui.value.copy(selectedMateriaPrima=v,error=null)};fun clearError(){_ui.value=_ui.value.copy(error=null)}
 fun saveMateriaPrima(n:String,d:String,c:String,u:String,p:String){val q=c.toDoubleOrNull();if(n.isBlank()||q==null||q<0){_ui.value=_ui.value.copy(error="Datos inválidos");return};viewModelScope.launch{try{val old=_ui.value.selectedMateriaPrima;AuxiliaryRepository.saveMaterial(MateriaPrima(old?.id?:UUID.randomUUID().toString(),n,d,q,u,p));_ui.value=MateriaPrimaUiState();refresh()}catch(e:Exception){_ui.value=_ui.value.copy(error=e.message)}}}
 fun deleteMateriaPrima(v:MateriaPrima)=viewModelScope.launch{try{AuxiliaryRepository.deleteMaterial(v.id);refresh()}catch(e:Exception){_ui.value=_ui.value.copy(error=e.message)}}
 fun usarMateriaPrima(id:String,s:String){val q=s.toDoubleOrNull();if(q==null||q<=0){_ui.value=_ui.value.copy(error="Cantidad inválida");return};viewModelScope.launch{try{AuxiliaryRepository.consumeMaterial(UUID.randomUUID().toString(),id,q);refresh()}catch(e:Exception){_ui.value=_ui.value.copy(error=e.message)}}}
}
