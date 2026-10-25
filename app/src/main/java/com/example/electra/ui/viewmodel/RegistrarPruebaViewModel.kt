package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.PruebasRepository
import com.example.electra.device.Coordenadas
import com.example.electra.device.ProveedorUbicacion
import com.example.electra.device.ResultadoUbicacion
import com.example.electra.model.OpcionesPrueba
import com.example.electra.model.Prueba
import com.example.electra.validation.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class EstadoUbicacion { SIN_CAPTURAR, BUSCANDO, CAPTURADA, SIN_PERMISO, GPS_DESACTIVADO, NO_DISPONIBLE }

data class RegistrarPruebaUiState(
    val equipo: String,
    val tipoPrueba: String = OpcionesPrueba.TIPOS.first(),
    val fecha: String,
    val hora: String,
    val medicion: String = "",
    val unidad: String = OpcionesPrueba.UNIDADES.first(),
    val resultado: String = OpcionesPrueba.APROBADO,
    val observaciones: String = "",
    val fotoUri: String? = null,
    val coordenadas: Coordenadas? = null,
    val estadoUbicacion: EstadoUbicacion = EstadoUbicacion.SIN_CAPTURAR,
    val mostrarErrores: Boolean = false,
    val guardando: Boolean = false,
    val intentosFallidos: Int = 0,
    val idGuardado: Long? = null,
    val errorAlGuardar: Boolean = false,
    val ahora: Date = Date()
) {
    val rechazada get() = resultado == OpcionesPrueba.RECHAZADO

    val errorEquipo get() = if (mostrarErrores) Validaciones.equipo(equipo) else null
    val errorMedicion get() = if (mostrarErrores) Validaciones.medicion(medicion) else null
    val errorFecha get() = if (mostrarErrores) Validaciones.fechaPrueba(fecha, hora, ahora) else null
    val errorObservaciones get() = if (mostrarErrores) Validaciones.observacionesPrueba(observaciones, rechazada) else null

    val medicionValida get() = Validaciones.medicion(medicion) == null

    val formularioValido
        get() = Validaciones.equipo(equipo) == null &&
                Validaciones.medicion(medicion) == null &&
                Validaciones.fechaPrueba(fecha, hora, ahora) == null &&
                Validaciones.observacionesPrueba(observaciones, rechazada) == null
}

class RegistrarPruebaViewModel(
    private val pruebas: PruebasRepository,
    private val ubicacion: ProveedorUbicacion,
    codigoEquipo: String,
    private val reloj: () -> Date = { Date() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        reloj().let { ahora ->
            RegistrarPruebaUiState(
                equipo = codigoEquipo.trim(),
                fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(ahora),
                hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(ahora),
                ahora = ahora
            )
        }
    )
    val uiState: StateFlow<RegistrarPruebaUiState> = _uiState.asStateFlow()

    fun onEquipoChange(valor: String) = _uiState.update { it.copy(equipo = valor) }
    fun onTipoPruebaChange(valor: String) = _uiState.update { it.copy(tipoPrueba = valor) }
    fun onFechaChange(valor: String) = _uiState.update { it.copy(fecha = valor, ahora = reloj()) }
    fun onHoraChange(valor: String) = _uiState.update { it.copy(hora = valor, ahora = reloj()) }
    fun onMedicionChange(valor: String) = _uiState.update { it.copy(medicion = valor) }
    fun onUnidadChange(valor: String) = _uiState.update { it.copy(unidad = valor) }
    fun onResultadoChange(valor: String) = _uiState.update { it.copy(resultado = valor) }
    fun onObservacionesChange(valor: String) = _uiState.update { it.copy(observaciones = valor) }

    fun onFotoTomada(uri: String) = _uiState.update { it.copy(fotoUri = uri) }

    fun quitarFoto() = _uiState.update { it.copy(fotoUri = null) }

    fun onPermisoUbicacionDenegado() =
        _uiState.update { it.copy(estadoUbicacion = EstadoUbicacion.SIN_PERMISO) }

    fun capturarUbicacion() {
        if (_uiState.value.estadoUbicacion == EstadoUbicacion.BUSCANDO) return
        _uiState.update { it.copy(estadoUbicacion = EstadoUbicacion.BUSCANDO) }
        viewModelScope.launch {
            val resultado = ubicacion.obtener()
            _uiState.update {
                when (resultado) {
                    is ResultadoUbicacion.Exito ->
                        it.copy(coordenadas = resultado.coordenadas, estadoUbicacion = EstadoUbicacion.CAPTURADA)
                    ResultadoUbicacion.SinPermiso -> it.copy(estadoUbicacion = EstadoUbicacion.SIN_PERMISO)
                    ResultadoUbicacion.GpsDesactivado -> it.copy(estadoUbicacion = EstadoUbicacion.GPS_DESACTIVADO)
                    ResultadoUbicacion.NoDisponible -> it.copy(estadoUbicacion = EstadoUbicacion.NO_DISPONIBLE)
                }
            }
        }
    }

    fun guardar() {
        val estado = _uiState.value.copy(mostrarErrores = true, ahora = reloj(), errorAlGuardar = false)
        if (estado.guardando) return

        if (!estado.formularioValido) {
            _uiState.value = estado.copy(intentosFallidos = estado.intentosFallidos + 1)
            return
        }

        val prueba = Prueba(
            equipo = estado.equipo.trim(),
            tipoPrueba = estado.tipoPrueba,
            fecha = estado.fecha,
            hora = estado.hora,
            medicion = estado.medicion.trim().replace(',', '.'),
            unidad = estado.unidad,
            resultado = estado.resultado,
            observaciones = estado.observaciones.trim(),
            fotoUri = estado.fotoUri,
            latitud = estado.coordenadas?.latitud,
            longitud = estado.coordenadas?.longitud
        )

        _uiState.value = estado.copy(guardando = true)
        viewModelScope.launch {
            val id = pruebas.insertar(prueba)
            _uiState.update {
                it.copy(
                    guardando = false,
                    idGuardado = id,
                    errorAlGuardar = id == null,
                    intentosFallidos = if (id == null) it.intentosFallidos + 1 else it.intentosFallidos
                )
            }
        }
    }

    fun guardadoConsumido() {
        _uiState.update { it.copy(idGuardado = null, errorAlGuardar = false) }
    }
}
