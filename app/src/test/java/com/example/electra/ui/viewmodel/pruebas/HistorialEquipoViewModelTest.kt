package com.example.electra.ui.viewmodel.pruebas

import com.example.electra.ui.viewmodel.HistorialEquipoViewModel
import com.example.electra.ui.viewmodel.MainDispatcherRule
import com.example.electra.ui.viewmodel.prueba
import com.example.electra.data.PruebasRepositoryEnMemoria
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HistorialEquipoViewModelTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var viewModel: HistorialEquipoViewModel

    @Before
    fun preparar() {
        val repositorio = PruebasRepositoryEnMemoria(
            listOf(prueba(1, "TR-001"), prueba(2, "RL-004"), prueba(3, "TR-001"))
        )
        viewModel = HistorialEquipoViewModel(repositorio, "TR-001")
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun idsMostrados() = viewModel.uiState.value.pruebas.map { it.id }

    @Test
    fun cargar_conPruebasDeVariosEquipos_muestraSoloLasDelEquipo() {
        viewModel.cargar()

        assertEquals(listOf(3L, 1L), idsMostrados())
    }
}
