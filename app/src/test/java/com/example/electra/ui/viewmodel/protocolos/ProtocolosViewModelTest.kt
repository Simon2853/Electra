package com.example.electra.ui.viewmodel.protocolos

import com.example.electra.ui.viewmodel.ProtocolosViewModel
import com.example.electra.ui.viewmodel.MainDispatcherRule
import com.example.electra.ui.viewmodel.protocolo
import com.example.electra.data.ProtocolosRepositoryEnMemoria
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProtocolosViewModelTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var viewModel: ProtocolosViewModel

    @Before
    fun preparar() {
        val repositorio = ProtocolosRepositoryEnMemoria(
            listOf(protocolo(1, "#000122"), protocolo(2, "#000123"))
        )
        viewModel = ProtocolosViewModel(repositorio)
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun numerosMostrados() = viewModel.protocolos.value.map { it.numero }

    @Test
    fun cargar_conProtocolosGuardados_muestraLosMasRecientesPrimero() {
        viewModel.cargar()

        assertEquals(listOf("#000123", "#000122"), numerosMostrados())
    }
}
