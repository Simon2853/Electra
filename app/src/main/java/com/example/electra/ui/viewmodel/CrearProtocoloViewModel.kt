package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.ProtocolosRepository
import com.example.electra.data.PruebasRepository
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.EstadosProtocolo
import com.example.electra.model.Protocolo
import com.example.electra.validation.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** ERROR = el servidor no guardó el protocolo (sin conexión o lo rechazó). */
enum class ResultadoProtocolo { CAMPOS_INCOMPLETOS, ERROR, EXITO }

/**
 * El número y el estado inicial los asigna protocolos-service al guardar, para que el número sea
 * único en toda la empresa. [numero] es solo una referencia del que probablemente le toque.
 */
data class CrearProtocoloUiState(
    val numero: String = "",
    val equipo: String,
    val estado: String = EstadosProtocolo.EN_TERRENO,
    val responsable: String = "",
    val observaciones: String = "",
    val fecha: String,
    val equipos: List<String> = emptyList(),
    val mostrarErrores: Boolean = false,
    val guardando: Boolean = false,
    val intentosFallidos: Int = 0,
    val resultado: ResultadoProtocolo? = null
) {
    val numeroReferencial get() = numero.ifBlank { "Se asigna al guardar" }

    val errorResponsable get() = if (mostrarErrores) Validaciones.responsable(responsable) else null
    val errorObservaciones get() = Validaciones.observaciones(observaciones)

    val responsableValido get() = Validaciones.responsable(responsable) == null

    val formularioValido get() = responsableValido && errorObservaciones == null
}

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
            val equipos = (CatalogoEquipos.CODIGOS + pruebas.equiposConRegistros()).distinct()
            val numeroSugerido = protocolos.siguienteNumero()

            _uiState.update {
                it.copy(
                    equipos = equipos,
                    equipo = if (codigoEquipoInicial.isBlank()) equipos.first() else it.equipo,
                    numero = numeroSugerido
                )
            }
        }
    }

    fun onEquipoChange(valor: String) = _uiState.update { it.copy(equipo = valor) }
    fun onResponsableChange(valor: String) = _uiState.update { it.copy(responsable = valor) }
    fun onObservacionesChange(valor: String) = _uiState.update { it.copy(observaciones = valor) }

    fun guardar() {
        val estado = _uiState.value.copy(mostrarErrores = true)
        if (estado.guardando) return

        if (!estado.formularioValido) {
            _uiState.value = estado.copy(
                intentosFallidos = estado.intentosFallidos + 1,
                resultado = ResultadoProtocolo.CAMPOS_INCOMPLETOS
            )
            return
        }

        val nuevo = Protocolo(
            numero = estado.numero, // referencial: el servidor asigna el definitivo
            equipo = estado.equipo.trim(),
            fecha = estado.fecha.trim(),
            estado = EstadosProtocolo.EN_TERRENO,
            responsable = estado.responsable.trim(),
            observaciones = estado.observaciones.trim()
        )

        _uiState.value = estado.copy(guardando = true)
        viewModelScope.launch {
            val ok = protocolos.insertar(nuevo)
            _uiState.update {
                it.copy(
                    guardando = false,
                    intentosFallidos = if (ok) it.intentosFallidos else it.intentosFallidos + 1,
                    resultado = if (ok) ResultadoProtocolo.EXITO else ResultadoProtocolo.ERROR
                )
            }
        }
    }

    fun resultadoMostrado() {
        _uiState.update { it.copy(resultado = null) }
    }
}
