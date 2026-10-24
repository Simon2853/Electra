package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.ProtocolosRepository
import com.example.electra.model.Protocolo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MensajeProtocolo { OBSERVACION_ACTUALIZADA, ERROR_ACTUALIZAR }

data class DetalleProtocoloUiState(
    val cargando: Boolean = true,
    val protocolo: Protocolo? = null,
    val mensaje: MensajeProtocolo? = null
)

class DetalleProtocoloViewModel(
    private val repositorio: ProtocolosRepository,
    private val numeroProtocolo: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleProtocoloUiState())
    val uiState: StateFlow<DetalleProtocoloUiState> = _uiState.asStateFlow()

    /** La pantalla lo llama en cada ON_RESUME. */
    fun cargar() {
        viewModelScope.launch { recargar() }
    }

    fun formalizar() {
        val protocolo = _uiState.value.protocolo ?: return
        viewModelScope.launch {
            repositorio.cambiarEstado(protocolo.numero, "Formalizado")
            recargar()
        }
    }

    fun actualizarObservaciones(texto: String) {
        viewModelScope.launch {
            val ok = repositorio.actualizarObservaciones(numeroProtocolo, texto)
            if (ok) recargar()
            _uiState.update {
                it.copy(mensaje = if (ok) MensajeProtocolo.OBSERVACION_ACTUALIZADA else MensajeProtocolo.ERROR_ACTUALIZAR)
            }
        }
    }

    fun mensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    private suspend fun recargar() {
        val protocolo = repositorio.buscar(numeroProtocolo)
        _uiState.update { it.copy(cargando = false, protocolo = protocolo) }
    }
}
