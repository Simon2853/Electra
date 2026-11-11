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
    val guardado: Boolean = false,
    val errorFoto: String? = null
) {
    val errorNombre: String? get() = if (mostrarErrores) Validaciones.nombre(nombre) else null
    val nombreValido get() = Validaciones.nombre(nombre) == null
}

/**
 * @param eliminarArchivo borra una foto por su Uri. Se usa para no dejar archivos huérfanos:
 * la foto anterior al guardar una nueva, y la foto nueva si el técnico sale sin guardar.
 */
class EditarPerfilViewModel(
    private val usuarios: UsuariosRepository,
    private val sesion: SesionRepository,
    private val eliminarArchivo: (String) -> Unit = {}
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState.asStateFlow()

    // Foto que está guardada en la cuenta; cualquier otra en el estado es temporal hasta "Guardar".
    private var fotoGuardada: String? = null

    init {
        viewModelScope.launch {
            val usuario = sesion.usuarioActual().orEmpty()
            val nombre = usuarios.nombre(usuario).orEmpty()
            val avatar = usuarios.avatar(usuario)
            fotoGuardada = avatar.foto
            _uiState.update { it.copy(usuario = usuario, nombre = nombre, avatar = avatar) }
        }
    }

    fun onNombreChange(valor: String) = _uiState.update { it.copy(nombre = valor) }
    fun onColorChange(clave: String) = _uiState.update { it.copy(avatar = it.avatar.copy(color = clave)) }
    fun onFondoChange(clave: String) = _uiState.update { it.copy(avatar = it.avatar.copy(fondo = clave)) }

    fun onFotoElegida(uri: String) {
        descartarFotoTemporal()
        _uiState.update { it.copy(avatar = it.avatar.copy(foto = uri), errorFoto = null) }
    }

    fun onQuitarFoto() {
        descartarFotoTemporal()
        _uiState.update { it.copy(avatar = it.avatar.copy(foto = null), errorFoto = null) }
    }

    fun onErrorFoto(mensaje: String) = _uiState.update { it.copy(errorFoto = mensaje) }

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
            if (ok) {
                val anterior = fotoGuardada
                if (anterior != null && anterior != estado.avatar.foto) eliminarArchivo(anterior)
                fotoGuardada = estado.avatar.foto
            }
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

    override fun onCleared() {
        descartarFotoTemporal()
    }

    private fun descartarFotoTemporal() {
        val actual = _uiState.value.avatar.foto
        if (actual != null && actual != fotoGuardada) eliminarArchivo(actual)
    }
}
