package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.PruebasRepository
import com.example.electra.model.Prueba
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetallePruebaUiState(
    val cargando: Boolean = true,
    val prueba: Prueba? = null
)

class DetallePruebaViewModel(
    private val pruebas: PruebasRepository,
    idPrueba: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetallePruebaUiState())
    val uiState: StateFlow<DetallePruebaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = DetallePruebaUiState(cargando = false, prueba = pruebas.buscarPorId(idPrueba))
        }
    }
}
