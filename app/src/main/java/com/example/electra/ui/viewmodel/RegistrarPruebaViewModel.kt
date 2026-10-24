package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.PruebasRepository
import com.example.electra.model.Prueba
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ResultadoGuardado { MEDICION_VACIA, EXITO, ERROR }

data class RegistrarPruebaUiState(
    val guardando: Boolean = false,
    val resultado: ResultadoGuardado? = null
)

class RegistrarPruebaViewModel(
    private val pruebas: PruebasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrarPruebaUiState())
    val uiState: StateFlow<RegistrarPruebaUiState> = _uiState.asStateFlow()

    fun guardar(prueba: Prueba) {
        if (_uiState.value.guardando) return

        if (prueba.medicion.isBlank()) {
            _uiState.update { it.copy(resultado = ResultadoGuardado.MEDICION_VACIA) }
            return
        }

        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            // trim() para evitar espacios accidentales en el código del equipo
            val id = pruebas.insertar(prueba.copy(equipo = prueba.equipo.trim()))
            _uiState.update {
                it.copy(
                    guardando = false,
                    resultado = if (id != null) ResultadoGuardado.EXITO else ResultadoGuardado.ERROR
                )
            }
        }
    }

    fun resultadoMostrado() {
        _uiState.update { it.copy(resultado = null) }
    }
}
