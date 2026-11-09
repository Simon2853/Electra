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

data class CambiarContrasenaUiState(
    val usuario: String = "",
    val nombre: String = "",
    val actual: String = "",
    val nueva: String = "",
    val confirmar: String = "",
    val mostrarErrores: Boolean = false,
    val actualIncorrecta: Boolean = false,
    val guardando: Boolean = false,
    val intentosFallidos: Int = 0,
    val cambiada: Boolean = false
) {
    val errorActual: String?
        get() = when {
            !mostrarErrores -> null
            actualIncorrecta -> "La contraseña actual no es correcta."
            else -> Validaciones.contrasenaLogin(actual)
        }

    val errorNueva: String? get() = if (mostrarErrores) reglaNueva() else null

    val errorConfirmar: String?
        get() = if (mostrarErrores) Validaciones.confirmacion(nueva, confirmar) else null

    val requisitos: List<Pair<String, Boolean>> get() = Validaciones.requisitosContrasena(nueva)

    val nuevaValida get() = reglaNueva() == null
    val confirmarValido get() = nuevaValida && Validaciones.confirmacion(nueva, confirmar) == null

    val formularioValido get() = Validaciones.contrasenaLogin(actual) == null && nuevaValida && confirmarValido

    private fun reglaNueva(): String? =
        Validaciones.contrasenaRegistro(nombre, usuario, nueva)
            ?: if (nueva == actual) "La nueva contraseña debe ser distinta de la actual." else null
}

class CambiarContrasenaViewModel(
    private val usuarios: UsuariosRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CambiarContrasenaUiState())
    val uiState: StateFlow<CambiarContrasenaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = sesion.usuarioActual().orEmpty()
            val nombre = usuarios.nombre(usuario).orEmpty()
            _uiState.update { it.copy(usuario = usuario, nombre = nombre) }
        }
    }

    fun onActualChange(valor: String) = _uiState.update { it.copy(actual = valor, actualIncorrecta = false) }
    fun onNuevaChange(valor: String) = _uiState.update { it.copy(nueva = valor) }
    fun onConfirmarChange(valor: String) = _uiState.update { it.copy(confirmar = valor) }

    fun cambiar() {
        val estado = _uiState.value.copy(mostrarErrores = true)
        if (estado.guardando) return

        if (!estado.formularioValido) {
            _uiState.value = estado.copy(intentosFallidos = estado.intentosFallidos + 1)
            return
        }

        _uiState.value = estado.copy(guardando = true)
        viewModelScope.launch {
            val ok = usuarios.cambiarContrasena(estado.usuario, estado.actual, estado.nueva)
            _uiState.update {
                it.copy(
                    guardando = false,
                    actualIncorrecta = !ok,
                    intentosFallidos = if (ok) it.intentosFallidos else it.intentosFallidos + 1,
                    cambiada = ok
                )
            }
        }
    }

    fun cambiadaConsumida() {
        _uiState.update { it.copy(cambiada = false) }
    }
}
