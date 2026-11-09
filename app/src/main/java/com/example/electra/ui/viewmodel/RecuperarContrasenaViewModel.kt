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

data class RecuperarContrasenaUiState(
    val usuario: String = "",
    val nombre: String = "",
    val contrasena: String = "",
    val confirmar: String = "",
    val mostrarErrores: Boolean = false,
    val datosNoCoinciden: Boolean = false,
    val guardando: Boolean = false,
    val intentosFallidos: Int = 0,
    val restablecida: Boolean = false
) {
    val errorUsuario: String? get() = if (mostrarErrores) Validaciones.usuarioLogin(usuario) else null
    val errorNombre: String? get() = if (mostrarErrores) Validaciones.nombre(nombre) else null

    val errorContrasena: String?
        get() = if (mostrarErrores) Validaciones.contrasenaRegistro(nombre, usuario, contrasena) else null

    val errorConfirmar: String?
        get() = if (mostrarErrores) Validaciones.confirmacion(contrasena, confirmar) else null

    val requisitos: List<Pair<String, Boolean>> get() = Validaciones.requisitosContrasena(contrasena)

    val contrasenaValida get() = Validaciones.contrasenaRegistro(nombre, usuario, contrasena) == null
    val confirmarValido get() = contrasenaValida && Validaciones.confirmacion(contrasena, confirmar) == null

    val formularioValido
        get() = Validaciones.usuarioLogin(usuario) == null && Validaciones.nombre(nombre) == null &&
                contrasenaValida && confirmarValido
}

class RecuperarContrasenaViewModel(
    private val usuarios: UsuariosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarContrasenaUiState())
    val uiState: StateFlow<RecuperarContrasenaUiState> = _uiState.asStateFlow()

    fun onUsuarioChange(valor: String) = _uiState.update { it.copy(usuario = valor, datosNoCoinciden = false) }
    fun onNombreChange(valor: String) = _uiState.update { it.copy(nombre = valor, datosNoCoinciden = false) }
    fun onContrasenaChange(valor: String) = _uiState.update { it.copy(contrasena = valor) }
    fun onConfirmarChange(valor: String) = _uiState.update { it.copy(confirmar = valor) }

    fun restablecer() {
        val estado = _uiState.value.copy(mostrarErrores = true)
        if (estado.guardando) return

        if (!estado.formularioValido) {
            _uiState.value = estado.copy(intentosFallidos = estado.intentosFallidos + 1)
            return
        }

        _uiState.value = estado.copy(guardando = true)
        viewModelScope.launch {
            val ok = usuarios.restablecer(estado.usuario.trim(), estado.nombre.trim(), estado.contrasena)
            _uiState.update {
                it.copy(
                    guardando = false,
                    datosNoCoinciden = !ok,
                    intentosFallidos = if (ok) it.intentosFallidos else it.intentosFallidos + 1,
                    restablecida = ok
                )
            }
        }
    }

    fun restablecidaConsumida() {
        _uiState.update { it.copy(restablecida = false) }
    }
}
