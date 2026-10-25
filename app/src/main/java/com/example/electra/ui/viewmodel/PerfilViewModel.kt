package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.ProtocolosRepository
import com.example.electra.data.PruebasRepository
import com.example.electra.data.SesionRepository
import com.example.electra.data.UsuariosRepository
import com.example.electra.model.EstadosProtocolo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val nombre: String = "",
    val usuario: String = "",
    val pruebasGuardadas: Int = 0,
    val protocolosPendientes: Int = 0,
    val sesionCerrada: Boolean = false
)

class PerfilViewModel(
    private val usuarios: UsuariosRepository,
    private val sesion: SesionRepository,
    private val pruebas: PruebasRepository,
    private val protocolos: ProtocolosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = sesion.usuarioActual().orEmpty()
            _uiState.update {
                it.copy(
                    usuario = usuario,
                    nombre = usuarios.nombre(usuario) ?: usuario.ifBlank { "Técnico Electra" },
                    pruebasGuardadas = pruebas.listarTodas().size,
                    protocolosPendientes = protocolos.listar().count { p -> p.estado == EstadosProtocolo.EN_TERRENO }
                )
            }
        }
    }

    fun cerrarSesion() {
        sesion.cerrar()
        _uiState.update { it.copy(sesionCerrada = true) }
    }
}
