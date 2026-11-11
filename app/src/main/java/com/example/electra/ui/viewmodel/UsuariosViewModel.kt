package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.SesionRepository
import com.example.electra.data.UsuariosRepository
import com.example.electra.data.rolActual
import com.example.electra.model.Rol
import com.example.electra.model.UsuarioResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MensajeUsuarios { ROL_ACTUALIZADO, PROPIO_ROL, SIN_PERMISO, ERROR }

data class UsuariosUiState(
    val cargando: Boolean = true,
    val usuarioActual: String = "",
    val rolActual: Rol? = null,
    val usuarios: List<UsuarioResumen> = emptyList(),
    /** Usuario al que se le está cambiando el rol (diálogo abierto). */
    val editando: UsuarioResumen? = null,
    val rolElegido: Rol = Rol.POR_DEFECTO,
    val mensaje: MensajeUsuarios? = null
) {
    val conteoPorRol: Map<Rol, Int> get() = Rol.entries.associateWith { rol -> usuarios.count { it.rol == rol } }
}

/** Gestión de cuentas del dispositivo. Solo el administrador puede cambiar roles, y nunca el suyo propio
 *  (así siempre queda al menos un administrador y nadie se bloquea a sí mismo). */
class UsuariosViewModel(
    private val usuarios: UsuariosRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuariosUiState())
    val uiState: StateFlow<UsuariosUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch { recargar() }
    }

    fun abrirCambioRol(usuario: UsuarioResumen) {
        if (usuario.usuario == _uiState.value.usuarioActual) {
            _uiState.update { it.copy(mensaje = MensajeUsuarios.PROPIO_ROL) }
            return
        }
        _uiState.update { it.copy(editando = usuario, rolElegido = usuario.rol) }
    }

    fun onRolElegido(rol: Rol) = _uiState.update { it.copy(rolElegido = rol) }

    fun cancelarCambioRol() = _uiState.update { it.copy(editando = null) }

    fun confirmarCambioRol() {
        val estado = _uiState.value
        val destino = estado.editando ?: return
        viewModelScope.launch {
            val mensaje = when {
                !usuarios.rolActual(sesion).puedeGestionarUsuarios -> MensajeUsuarios.SIN_PERMISO
                destino.usuario == sesion.usuarioActual() -> MensajeUsuarios.PROPIO_ROL
                usuarios.actualizarRol(destino.usuario, estado.rolElegido) -> MensajeUsuarios.ROL_ACTUALIZADO
                else -> MensajeUsuarios.ERROR
            }
            recargar()
            _uiState.update { it.copy(editando = null, mensaje = mensaje) }
        }
    }

    fun mensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    private suspend fun recargar() {
        val actual = sesion.usuarioActual().orEmpty()
        val rol = usuarios.rolActual(sesion)
        val lista = if (rol.puedeGestionarUsuarios) usuarios.listar() else emptyList()
        _uiState.update { it.copy(cargando = false, usuarioActual = actual, rolActual = rol, usuarios = lista) }
    }
}
