package com.example.fashgo.ui.screens.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.database.FashGoDatabase
import com.example.fashgo.data.entity.Usuario
import com.example.fashgo.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedInUser: Usuario? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UsuarioRepository
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        val db = FashGoDatabase.getDatabase(application)
        repository = UsuarioRepository(db.usuarioDao())
    }

    fun login(nombre: String, password: String) {
        if (nombre.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Ingrese usuario y contraseña")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val user = repository.login(nombre, password)
            if (user != null) {
                _uiState.value = _uiState.value.copy(isLoading = false, loggedInUser = user)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Usuario o contraseña incorrectos"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
