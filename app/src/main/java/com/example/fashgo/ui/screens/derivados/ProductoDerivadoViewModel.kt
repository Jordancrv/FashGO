package com.example.fashgo.ui.screens.derivados

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.database.FashGoDatabase
import com.example.fashgo.data.entity.ProductoDerivado
import com.example.fashgo.data.repository.ProductoDerivadoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductoDerivadoUiState(
    val selected: ProductoDerivado? = null,
    val error: String? = null
)

class ProductoDerivadoViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ProductoDerivadoRepository(FashGoDatabase.getDatabase(application).productoDerivadoDao())

    val derivados: StateFlow<List<ProductoDerivado>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uiState = MutableStateFlow(ProductoDerivadoUiState())
    val uiState: StateFlow<ProductoDerivadoUiState> = _uiState

    fun select(item: ProductoDerivado?) {
        _uiState.value = _uiState.value.copy(selected = item, error = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun save(
        tipoPrenda: String,
        subcategoria: String,
        color: String,
        talla: String,
        cantidad: String,
        precioUnitario: String,
        ubicacion: String,
        nivel: String,
        zonaAuxiliar: String,
        origen: String,
        notas: String
    ): Boolean {
        val cantidadVal = cantidad.toIntOrNull()
        val precioVal = precioUnitario.toDoubleOrNull()
        val zonaVal = zonaAuxiliar.toIntOrNull()
        if (tipoPrenda.isBlank() || subcategoria.isBlank() || color.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Completa tipo de prenda, subcategoria y color.")
            return false
        }
        if (cantidadVal == null || cantidadVal <= 0 || precioVal == null || precioVal <= 0) {
            _uiState.value = _uiState.value.copy(error = "Cantidad y precio deben ser validos.")
            return false
        }

        viewModelScope.launch {
            val current = _uiState.value.selected
            val item = ProductoDerivado(
                id = current?.id ?: 0,
                tipoPrenda = tipoPrenda,
                subcategoria = subcategoria,
                color = color,
                talla = talla.ifBlank { "Estandar" },
                cantidad = cantidadVal,
                precioUnitario = precioVal,
                ubicacion = ubicacion.ifBlank { if (zonaVal != null) "Zona Auxiliar $zonaVal" else "Zona Auxiliar" },
                nivel = nivel,
                zonaAuxiliar = zonaVal,
                origen = origen,
                notas = notas,
                rotacion = current?.rotacion ?: "Baja",
                decisionSugerida = current?.decisionSugerida ?: "Derivado",
                fechaRegistro = current?.fechaRegistro ?: System.currentTimeMillis()
            )
            if (current == null) repo.insert(item) else repo.update(item)
            _uiState.value = ProductoDerivadoUiState()
        }
        return true
    }

    fun delete(item: ProductoDerivado) {
        viewModelScope.launch {
            repo.delete(item)
            _uiState.value = ProductoDerivadoUiState()
        }
    }
}
