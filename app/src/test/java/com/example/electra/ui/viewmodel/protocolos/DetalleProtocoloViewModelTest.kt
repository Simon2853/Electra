package com.example.electra.ui.viewmodel.protocolos

import com.example.electra.ui.viewmodel.DetalleProtocoloViewModel
import com.example.electra.ui.viewmodel.MainDispatcherRule
import com.example.electra.ui.viewmodel.protocolo
import com.example.electra.data.ProtocolosRepositoryEnMemoria
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.model.Rol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DetalleProtocoloViewModelTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var viewModel: DetalleProtocoloViewModel

    @Before
    fun preparar() {
        val repositorio = ProtocolosRepositoryEnMemoria(listOf(protocolo(1, "#000122")))
        viewModel = DetalleProtocoloViewModel(
            repositorio, PruebasRepositoryEnMemoria(), "000122"
        ) { Rol.SUPERVISOR }
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun abrirProtocolo() {
        viewModel.cargar()
    }

    @Test
    fun formalizar_comoSupervisor_cambiaElEstado() {
        abrirProtocolo()

        viewModel.formalizar()

        assertTrue(viewModel.uiState.value.formalizado)
        assertEquals("Formalizado", viewModel.uiState.value.protocolo?.estado)
    }
}
