package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.ProtocolosRepository
import com.example.electra.data.PruebasRepository
import com.example.electra.model.EstadosProtocolo
import com.example.electra.model.Protocolo
import com.example.electra.model.Prueba
import com.example.electra.model.Respaldo
import com.example.electra.validation.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MensajeProtocolo { OBSERVACION_ACTUALIZADA, ERROR_ACTUALIZAR, FORMALIZADO }

data class DetalleProtocoloUiState(
    val cargando: Boolean = true,
    val protocolo: Protocolo? = null,
    val pruebas: List<Prueba> = emptyList(),
    val editandoObservacion: Boolean = false,
    val textoObservacion: String = "",
    val mensaje: MensajeProtocolo? = null
) {
    val formalizado get() = protocolo?.estado.equals(EstadosProtocolo.FORMALIZADO, ignoreCase = true)
    val errorObservacion get() = Validaciones.observaciones(textoObservacion)
}

class DetalleProtocoloViewModel(
    private val repositorio: ProtocolosRepository,
    private val pruebas: PruebasRepository,
    private val numeroProtocolo: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleProtocoloUiState())
    val uiState: StateFlow<DetalleProtocoloUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch { recargar() }
    }

    fun formalizar() {
        val protocolo = _uiState.value.protocolo ?: return
        viewModelScope.launch {
            repositorio.cambiarEstado(protocolo.numero, EstadosProtocolo.FORMALIZADO)
            recargar()
            _uiState.update { it.copy(mensaje = MensajeProtocolo.FORMALIZADO) }
        }
    }

    fun abrirEdicion() = _uiState.update {
        it.copy(editandoObservacion = true, textoObservacion = it.protocolo?.observaciones.orEmpty())
    }

    fun onObservacionChange(texto: String) = _uiState.update { it.copy(textoObservacion = texto) }

    fun cancelarEdicion() = _uiState.update { it.copy(editandoObservacion = false) }

    fun guardarObservacion() {
        val estado = _uiState.value
        if (estado.errorObservacion != null) return
        actualizarObservaciones(estado.textoObservacion)
    }

    fun actualizarObservaciones(texto: String) {
        viewModelScope.launch {
            val ok = repositorio.actualizarObservaciones(numeroProtocolo, texto)
            if (ok) recargar()
            _uiState.update {
                it.copy(
                    editandoObservacion = !ok && it.editandoObservacion,
                    mensaje = if (ok) MensajeProtocolo.OBSERVACION_ACTUALIZADA else MensajeProtocolo.ERROR_ACTUALIZAR
                )
            }
        }
    }

    fun mensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun textoRespaldo(): String? {
        val estado = _uiState.value
        return estado.protocolo?.let { Respaldo.deProtocolo(it, estado.pruebas) }
    }

    private suspend fun recargar() {
        val protocolo = repositorio.buscar(numeroProtocolo)
        val delEquipo = protocolo?.let { pruebas.listarPorEquipo(it.equipo).take(LIMITE_RECIENTES_POR_EQUIPO) }
        _uiState.update {
            it.copy(cargando = false, protocolo = protocolo, pruebas = delEquipo.orEmpty())
        }
    }
}
