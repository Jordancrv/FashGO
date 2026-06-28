package com.example.fashgo.ui.screens.materiaprima

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.database.FashGoDatabase
import com.example.fashgo.data.entity.MateriaPrima
import com.example.fashgo.data.repository.MateriaPrimaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MateriaPrimaUiState(
    val selectedMateriaPrima: MateriaPrima? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class MateriaPrimaViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FashGoDatabase.getDatabase(application)
    private val repo = MateriaPrimaRepository(db.materiaPrimaDao())

    val materiaPrimaList: StateFlow<List<MateriaPrima>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uiState = MutableStateFlow(MateriaPrimaUiState())
    val uiState: StateFlow<MateriaPrimaUiState> = _uiState

    fun selectMateriaPrima(mp: MateriaPrima?) {
        _uiState.value = _uiState.value.copy(selectedMateriaPrima = mp, error = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun saveMateriaPrima(nombre: String, descripcion: String, cantidadStr: String, unidadMedida: String, proveedor: String) {
        val cantidad = cantidadStr.toDoubleOrNull()
        if (nombre.isBlank() || cantidad == null || cantidad < 0) {
            _uiState.value = _uiState.value.copy(error = "Datos inválidos. Verifica nombre y cantidad.")
            return
        }

        viewModelScope.launch {
            try {
                val mp = MateriaPrima(
                    id = _uiState.value.selectedMateriaPrima?.id ?: 0,
                    nombre = nombre,
                    descripcion = descripcion,
                    cantidad = cantidad,
                    unidadMedida = unidadMedida,
                    proveedor = proveedor
                )
                if (mp.id == 0L) {
                    repo.insert(mp)
                } else {
                    repo.update(mp)
                }
                _uiState.value = _uiState.value.copy(selectedMateriaPrima = null, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al guardar: ${e.message}")
            }
        }
    }

    fun deleteMateriaPrima(mp: MateriaPrima) {
        viewModelScope.launch {
            repo.delete(mp)
            _uiState.value = _uiState.value.copy(selectedMateriaPrima = null)
        }
    }

    fun usarMateriaPrima(id: Long, usoStr: String) {
        val uso = usoStr.toDoubleOrNull()
        if (uso == null || uso <= 0) {
            _uiState.value = _uiState.value.copy(error = "Cantidad a usar inválida.")
            return
        }
        viewModelScope.launch {
            val rows = repo.descontarMateriaPrima(id, uso)
            if (rows == 0) {
                _uiState.value = _uiState.value.copy(error = "No hay suficiente cantidad para descontar.")
            }
        }
    }
}
