package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.SesionRepository
import com.example.electra.data.UsuariosRepository
import com.example.electra.validation.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val usuario: String = "",
    val contrasena: String = "",
    val mostrarErrores: Boolean = false,
    val verificando: Boolean = false,
    val credencialesInvalidas: Boolean = false,
    val intentosFallidos: Int = 0,
    val ingresoExitoso: Boolean = false
) {
    val errorUsuario: String? get() = if (mostrarErrores) Validaciones.usuarioLogin(usuario) else null
    val errorContrasena: String? get() = if (mostrarErrores) Validaciones.contrasenaLogin(contrasena) else null
}

class LoginViewModel(
    private val usuarios: UsuariosRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsuarioChange(valor: String) =
        _uiState.update { it.copy(usuario = valor, credencialesInvalidas = false) }

    fun onContrasenaChange(valor: String) =
        _uiState.update { it.copy(contrasena = valor, credencialesInvalidas = false) }

    fun iniciarSesion() {
        val estado = _uiState.value.copy(mostrarErrores = true)
        if (estado.verificando) return

        if (estado.errorUsuario != null || estado.errorContrasena != null) {
            _uiState.value = estado.copy(intentosFallidos = estado.intentosFallidos + 1)
            return
        }

        val usuario = estado.usuario.trim()
        _uiState.value = estado.copy(verificando = true)
        viewModelScope.launch {
            val valido = usuarios.validar(usuario, estado.contrasena)
            if (valido) sesion.iniciar(usuario)
            _uiState.update {
                it.copy(
                    verificando = false,
                    credencialesInvalidas = !valido,
                    intentosFallidos = if (valido) it.intentosFallidos else it.intentosFallidos + 1,
                    ingresoExitoso = valido
                )
            }
        }
    }

    fun ingresoConsumido() {
        _uiState.update { it.copy(ingresoExitoso = false) }
    }
}
