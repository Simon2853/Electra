package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.ProtocolosRepository
import com.example.electra.data.PruebasRepository
import com.example.electra.data.SesionRepository
import com.example.electra.data.UsuariosRepository
import com.example.electra.data.rolActual
import com.example.electra.model.Avatar
import com.example.electra.model.Rol
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.EstadosProtocolo
import com.example.electra.model.Prueba
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val nombre: String = "Técnico",
    val nombreCompleto: String = "Técnico",
    val avatar: Avatar = Avatar(),
    val rol: Rol? = null,
    val totalPruebas: Int = 0,
    val protocolosPendientes: Int = 0,
    val totalProtocolos: Int = 0,
    val totalEquipos: Int = CatalogoEquipos.EQUIPOS.size,
    val recientes: List<Prueba> = emptyList()
)

class HomeViewModel(
    private val pruebas: PruebasRepository,
    private val protocolos: ProtocolosRepository,
    private val usuarios: UsuariosRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            val todas = pruebas.listarTodas()
            val listaProtocolos = protocolos.listar()
            val usuario = sesion.usuarioActual()
            val nombre = usuario?.let { usuarios.nombre(it) ?: it }
            val avatar = usuario?.let { usuarios.avatar(it) } ?: Avatar()
            val rol = usuarios.rolActual(sesion)

            _uiState.update {
                it.copy(
                    nombre = nombre?.substringBefore(' ') ?: it.nombre,
                    nombreCompleto = nombre ?: it.nombreCompleto,
                    avatar = avatar,
                    rol = rol,
                    totalPruebas = todas.size,
                    protocolosPendientes = listaProtocolos.count { p -> p.estado == EstadosProtocolo.EN_TERRENO },
                    totalProtocolos = listaProtocolos.size,
                    recientes = todas.take(3)
                )
            }
        }
    }
}
