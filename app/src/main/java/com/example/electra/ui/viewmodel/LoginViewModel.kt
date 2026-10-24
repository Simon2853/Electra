package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.UsuariosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val verificando: Boolean = false,
    val error: Boolean = false,
    val ingresoExitoso: Boolean = false
)

class LoginViewModel(
    private val usuarios: UsuariosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun iniciarSesion(usuario: String, contrasena: String) {
        if (_uiState.value.verificando) return

        // Conserva mayúsculas y minúsculas exactas
        val usuarioLimpio = usuario.trim()
        if (usuarioLimpio.isBlank()) {
            _uiState.update { it.copy(error = true) }
            return
        }

        _uiState.update { it.copy(verificando = true) }
        viewModelScope.launch {
            val valido = usuarios.validar(usuarioLimpio, contrasena)
            _uiState.update {
                it.copy(verificando = false, error = !valido, ingresoExitoso = valido)
            }
        }
    }

    fun ingresoConsumido() {
        _uiState.update { it.copy(ingresoExitoso = false) }
    }
}
