package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.SesionRepository
import com.example.electra.data.UsuariosRepository
import com.example.electra.model.Avatar
import com.example.electra.validation.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditarPerfilUiState(
    val usuario: String = "",
    val nombre: String = "",
    val avatar: Avatar = Avatar(),
    val mostrarErrores: Boolean = false,
    val guardando: Boolean = false,
    val intentosFallidos: Int = 0,
    val guardado: Boolean = false
) {
    val errorNombre: String? get() = if (mostrarErrores) Validaciones.nombre(nombre) else null
    val nombreValido get() = Validaciones.nombre(nombre) == null
}

class EditarPerfilViewModel(
    private val usuarios: UsuariosRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = sesion.usuarioActual().orEmpty()
            val nombre = usuarios.nombre(usuario).orEmpty()
            val avatar = usuarios.avatar(usuario)
            _uiState.update { it.copy(usuario = usuario, nombre = nombre, avatar = avatar) }
        }
    }

    fun onNombreChange(valor: String) = _uiState.update { it.copy(nombre = valor) }
    fun onIconoChange(clave: String) = _uiState.update { it.copy(avatar = it.avatar.copy(icono = clave)) }
    fun onColorChange(clave: String) = _uiState.update { it.copy(avatar = it.avatar.copy(color = clave)) }
    fun onFondoChange(clave: String) = _uiState.update { it.copy(avatar = it.avatar.copy(fondo = clave)) }

    fun guardar() {
        val estado = _uiState.value.copy(mostrarErrores = true)
        if (estado.guardando) return

        if (!estado.nombreValido) {
            _uiState.value = estado.copy(intentosFallidos = estado.intentosFallidos + 1)
            return
        }

        _uiState.value = estado.copy(guardando = true)
        viewModelScope.launch {
            val ok = usuarios.actualizarNombre(estado.usuario, estado.nombre.trim()) &&
                    usuarios.actualizarAvatar(estado.usuario, estado.avatar)
            _uiState.update {
                it.copy(
                    guardando = false,
                    intentosFallidos = if (ok) it.intentosFallidos else it.intentosFallidos + 1,
                    guardado = ok
                )
            }
        }
    }

    fun guardadoConsumido() {
        _uiState.update { it.copy(guardado = false) }
    }
}
