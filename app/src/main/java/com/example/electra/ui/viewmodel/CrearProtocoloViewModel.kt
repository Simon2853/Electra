package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.ProtocolosRepository
import com.example.electra.data.PruebasRepository
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.Protocolo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ResultadoProtocolo { CAMPOS_INCOMPLETOS, DUPLICADO, EXITO }

data class CrearProtocoloUiState(
    val numero: String = "",
    val equipo: String,
    val estado: String = "Registrado en terreno",
    val responsable: String = "",
    val observaciones: String = "",
    val fecha: String,
    val equipos: List<String> = emptyList(),
    val guardando: Boolean = false,
    val resultado: ResultadoProtocolo? = null
)

class CrearProtocoloViewModel(
    private val protocolos: ProtocolosRepository,
    private val pruebas: PruebasRepository,
    codigoEquipoInicial: String = "",
    fecha: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CrearProtocoloUiState(equipo = codigoEquipoInicial.ifBlank { "TR-001" }, fecha = fecha)
    )
    val uiState: StateFlow<CrearProtocoloUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val equipos = pruebas.equiposConRegistros().ifEmpty { CatalogoEquipos.CODIGOS_POR_DEFECTO }
            val numeroSugerido = "#${(protocolos.listar().size + 126).toString().padStart(6, '0')}"

            _uiState.update {
                it.copy(
                    equipos = equipos,
                    equipo = if (codigoEquipoInicial.isBlank()) equipos.first() else it.equipo,
                    // No pisa un número que el usuario ya haya empezado a escribir
                    numero = it.numero.ifBlank { numeroSugerido }
                )
            }
        }
    }

    fun onNumeroChange(valor: String) = _uiState.update { it.copy(numero = valor) }
    fun onEquipoChange(valor: String) = _uiState.update { it.copy(equipo = valor) }
    fun onEstadoChange(valor: String) = _uiState.update { it.copy(estado = valor) }
    fun onResponsableChange(valor: String) = _uiState.update { it.copy(responsable = valor) }
    fun onObservacionesChange(valor: String) = _uiState.update { it.copy(observaciones = valor) }

    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando) return

        if (estado.numero.isBlank() || estado.responsable.isBlank()) {
            _uiState.update { it.copy(resultado = ResultadoProtocolo.CAMPOS_INCOMPLETOS) }
            return
        }

        val numero = estado.numero.trim()
        val nuevo = Protocolo(
            numero = if (numero.startsWith("#")) numero else "#$numero",
            equipo = estado.equipo.trim(),
            fecha = estado.fecha.trim(),
            estado = estado.estado.trim(),
            responsable = estado.responsable.trim(),
            observaciones = estado.observaciones.trim()
        )

        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val ok = protocolos.insertar(nuevo)
            _uiState.update {
                it.copy(
                    guardando = false,
                    resultado = if (ok) ResultadoProtocolo.EXITO else ResultadoProtocolo.DUPLICADO
                )
            }
        }
    }

    fun resultadoMostrado() {
        _uiState.update { it.copy(resultado = null) }
    }
}
