package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.PruebasRepository
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.Prueba
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistorialEquipoUiState(
    val equipoSeleccionado: String,
    val equipos: List<String> = CatalogoEquipos.CODIGOS_POR_DEFECTO,
    val pruebas: List<Prueba> = emptyList(),
    val totalPruebas: Int = 0
)

class HistorialEquipoViewModel(
    private val repositorio: PruebasRepository,
    codigoEquipo: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialEquipoUiState(equipoSeleccionado = codigoEquipo))
    val uiState: StateFlow<HistorialEquipoUiState> = _uiState.asStateFlow()

    private var carga: Job? = null

    fun seleccionarEquipo(codigo: String) {
        _uiState.update { it.copy(equipoSeleccionado = codigo) }
        cargar()
    }

    fun cargar() {
        carga?.cancel()
        val equipo = _uiState.value.equipoSeleccionado.trim()
        carga = viewModelScope.launch {
            val equipos = repositorio.equiposConRegistros()
            val pruebas = repositorio.listarPorEquipo(equipo)
            _uiState.update {
                it.copy(
                    equipos = equipos.ifEmpty { it.equipos },
                    pruebas = pruebas.take(LIMITE_RECIENTES_POR_EQUIPO),
                    totalPruebas = pruebas.size
                )
            }
        }
    }
}
