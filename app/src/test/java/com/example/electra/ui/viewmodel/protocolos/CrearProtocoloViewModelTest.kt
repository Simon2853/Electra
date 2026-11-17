package com.example.electra.ui.viewmodel.protocolos

import com.example.electra.ui.viewmodel.CrearProtocoloViewModel
import com.example.electra.ui.viewmodel.MainDispatcherRule
import com.example.electra.ui.viewmodel.ResultadoProtocolo
import com.example.electra.ui.viewmodel.protocolo
import com.example.electra.data.ProtocolosRepositoryEnMemoria
import com.example.electra.data.PruebasRepositoryEnMemoria
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CrearProtocoloViewModelTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var viewModel: CrearProtocoloViewModel
    private lateinit var repositorio: ProtocolosRepositoryEnMemoria

    @Before
    fun preparar() {
        repositorio = ProtocolosRepositoryEnMemoria(listOf(protocolo(1, "#000122")))
        viewModel = CrearProtocoloViewModel(
            repositorio, PruebasRepositoryEnMemoria(), "", fecha = "23/10/2026"
        )
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun llenarFormularioValido() {
        viewModel.onResponsableChange("Camila Rojas")
    }

    @Test
    fun guardar_conDatosValidos_creaElProtocoloConElSiguienteNumero() {
        llenarFormularioValido()

        viewModel.guardar()

        val guardado = repositorio.protocolos.last()
        assertEquals(ResultadoProtocolo.EXITO, viewModel.uiState.value.resultado)
        assertEquals("#000123", guardado.numero)
        assertEquals("Camila Rojas", guardado.responsable)
    }
}
