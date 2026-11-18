package com.example.fashgo.ui.screens.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.cloud.CloudUser
import com.example.fashgo.data.cloud.SupabaseClient
import com.example.fashgo.data.offline.OfflineStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(val isLoading: Boolean = false, val error: String? = null, val loggedInUser: CloudUser? = null)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(nombre: String, password: String) {
        if (nombre.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Ingresa tu correo y contraseña")
            return
        }
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            try {
                val user=SupabaseClient.signIn(nombre,password)
                runCatching { OfflineStore.sync() }
                _uiState.value = LoginUiState(loggedInUser=user)
            } catch (e: Exception) {
                _uiState.value = LoginUiState(error = e.message ?: "No se pudo conectar. Revisa tu conexión e inténtalo de nuevo.")
            }
        }
    }

    fun clearMessages() { _uiState.value = _uiState.value.copy(error = null) }
    fun clearError() = clearMessages()
    fun logout() { SupabaseClient.clearSession(); _uiState.value = LoginUiState() }
}
