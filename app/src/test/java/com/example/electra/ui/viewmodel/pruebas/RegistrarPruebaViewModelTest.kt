package com.example.electra.ui.viewmodel.pruebas

import com.example.electra.ui.viewmodel.RegistrarPruebaViewModel
import com.example.electra.ui.viewmodel.MainDispatcherRule
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.device.Coordenadas
import com.example.electra.device.ResultadoUbicacion
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class RegistrarPruebaViewModelTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var viewModel: RegistrarPruebaViewModel
    private lateinit var repositorio: PruebasRepositoryEnMemoria

    @Before
    fun preparar() {
        val ahora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse("23/10/2026 12:00")!!
        repositorio = PruebasRepositoryEnMemoria()
        viewModel = RegistrarPruebaViewModel(
            repositorio,
            { ResultadoUbicacion.Exito(Coordenadas(-33.36, -70.68)) },
            "TR-001"
        ) { ahora }
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun llenarFormularioValido() {
        viewModel.onMedicionChange("12,5")
    }

    @Test
    fun guardar_conMedicionConComa_guardaLaMedicionNormalizada() {
        llenarFormularioValido()

        viewModel.guardar()

        val guardada = repositorio.pruebas.single()
        assertEquals("TR-001", guardada.equipo)
        assertEquals("12.5", guardada.medicion)
    }
}
