package com.example.fashgo.ui.screens.derivados
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.remote.AuxiliaryRepository
import com.example.fashgo.data.remote.model.ProductoDerivado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
data class ProductoDerivadoUiState(val selected:ProductoDerivado?=null,val error:String?=null)
class ProductoDerivadoViewModel(application:Application):AndroidViewModel(application){
 private val _items=MutableStateFlow<List<ProductoDerivado>>(emptyList());val derivados=_items.asStateFlow();private val _ui=MutableStateFlow(ProductoDerivadoUiState());val uiState=_ui.asStateFlow()
 init{refresh()};private fun refresh()=viewModelScope.launch{try{_items.value=AuxiliaryRepository.derivatives()}catch(e:Exception){_ui.value=_ui.value.copy(error=e.message)}}
 fun select(v:ProductoDerivado?){_ui.value=_ui.value.copy(selected=v,error=null)};fun clearError(){_ui.value=_ui.value.copy(error=null)}
 fun save(t:String,s:String,c:String,size:String,qty:String,price:String,loc:String,level:String,zone:String,origin:String,notes:String):Boolean{val q=qty.toIntOrNull();val p=price.toDoubleOrNull();if(t.isBlank()||s.isBlank()||c.isBlank()||q==null||q<=0||p==null||p<=0){_ui.value=_ui.value.copy(error="Completa los datos válidos");return false};val old=_ui.value.selected;viewModelScope.launch{try{AuxiliaryRepository.saveDerivative(ProductoDerivado(old?.id?:UUID.randomUUID().toString(),t,s,c,size.ifBlank{"Estandar"},q,p,old?.rotacion?:"Baja",old?.decisionSugerida?:"Derivado",loc.ifBlank{"Zona Auxiliar"},level,zone.toIntOrNull(),origin,notes));_ui.value=ProductoDerivadoUiState();refresh()}catch(e:Exception){_ui.value=_ui.value.copy(error=e.message)}};return true}
 fun delete(v:ProductoDerivado)=viewModelScope.launch{try{AuxiliaryRepository.deleteDerivative(v.id);refresh()}catch(e:Exception){_ui.value=_ui.value.copy(error=e.message)}}
}
