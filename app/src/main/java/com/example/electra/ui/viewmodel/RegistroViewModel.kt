package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.UsuariosRepository
import com.example.electra.validation.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroUiState(
    val nombre: String = "",
    val usuario: String = "",
    val contrasena: String = "",
    val confirmar: String = "",
    val mostrarErrores: Boolean = false,
    val usuarioOcupado: Boolean = false,
    val registrando: Boolean = false,
    val intentosFallidos: Int = 0,
    val registroExitoso: Boolean = false
) {
    val errorNombre: String? get() = if (mostrarErrores) Validaciones.nombre(nombre) else null

    val errorUsuario: String?
        get() = when {
            !mostrarErrores -> null
            usuarioOcupado -> "Ese usuario ya existe."
            else -> Validaciones.usuarioRegistro(usuario)
        }

    val errorContrasena: String?
        get() = if (mostrarErrores) Validaciones.contrasenaRegistro(nombre, usuario, contrasena) else null

    val errorConfirmar: String?
        get() = if (mostrarErrores) Validaciones.confirmacion(contrasena, confirmar) else null

    val requisitos: List<Pair<String, Boolean>> get() = Validaciones.requisitosContrasena(contrasena)

    val nombreValido get() = Validaciones.nombre(nombre) == null
    val usuarioValido get() = !usuarioOcupado && Validaciones.usuarioRegistro(usuario) == null
    val contrasenaValida get() = Validaciones.contrasenaRegistro(nombre, usuario, contrasena) == null
    val confirmarValido get() = contrasenaValida && Validaciones.confirmacion(contrasena, confirmar) == null

    val formularioValido get() = nombreValido && usuarioValido && contrasenaValida && confirmarValido
}

class RegistroViewModel(
    private val usuarios: UsuariosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onNombreChange(valor: String) = _uiState.update { it.copy(nombre = valor) }
    fun onUsuarioChange(valor: String) = _uiState.update { it.copy(usuario = valor, usuarioOcupado = false) }
    fun onContrasenaChange(valor: String) = _uiState.update { it.copy(contrasena = valor) }
    fun onConfirmarChange(valor: String) = _uiState.update { it.copy(confirmar = valor) }

    fun registrar() {
        val estado = _uiState.value.copy(mostrarErrores = true)
        if (estado.registrando) return

        if (!estado.formularioValido) {
            _uiState.value = estado.copy(intentosFallidos = estado.intentosFallidos + 1)
            return
        }

        _uiState.value = estado.copy(registrando = true)
        viewModelScope.launch {
            val ok = usuarios.registrar(estado.nombre.trim(), estado.usuario.trim(), estado.contrasena)
            _uiState.update {
                it.copy(
                    registrando = false,
                    usuarioOcupado = !ok,
                    intentosFallidos = if (ok) it.intentosFallidos else it.intentosFallidos + 1,
                    registroExitoso = ok
                )
            }
        }
    }

    fun registroConsumido() {
        _uiState.update { it.copy(registroExitoso = false) }
    }
}
