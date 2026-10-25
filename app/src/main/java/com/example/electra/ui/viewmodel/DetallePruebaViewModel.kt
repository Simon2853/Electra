package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.PruebasRepository
import com.example.electra.model.Prueba
import com.example.electra.model.Respaldo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Comparacion(
    val anterior: Prueba,
    val diferencia: Double,
    val porcentaje: Double?
)

data class DetallePruebaUiState(
    val cargando: Boolean = true,
    val prueba: Prueba? = null,
    val comparacion: Comparacion? = null
)

class DetallePruebaViewModel(
    private val pruebas: PruebasRepository,
    idPrueba: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetallePruebaUiState())
    val uiState: StateFlow<DetallePruebaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val prueba = pruebas.buscarPorId(idPrueba)
            val comparacion = prueba?.let { compararConAnterior(it, pruebas.listarPorEquipo(it.equipo)) }
            _uiState.value = DetallePruebaUiState(cargando = false, prueba = prueba, comparacion = comparacion)
        }
    }

    fun textoRespaldo(): String? = _uiState.value.prueba?.let(Respaldo::dePrueba)
}

fun compararConAnterior(actual: Prueba, historial: List<Prueba>): Comparacion? {
    val valorActual = actual.medicion.toDoubleOrNull() ?: return null
    val anterior = historial
        .filter { it.id < actual.id && it.tipoPrueba == actual.tipoPrueba && it.unidad == actual.unidad }
        .maxByOrNull { it.id } ?: return null
    val valorAnterior = anterior.medicion.toDoubleOrNull() ?: return null
    val diferencia = valorActual - valorAnterior
    val porcentaje = if (valorAnterior != 0.0) diferencia / valorAnterior * 100 else null
    return Comparacion(anterior, diferencia, porcentaje)
}
