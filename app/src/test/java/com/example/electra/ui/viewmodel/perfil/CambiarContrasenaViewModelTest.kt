package com.example.electra.ui.viewmodel.perfil

import com.example.electra.ui.viewmodel.CambiarContrasenaViewModel
import com.example.electra.ui.viewmodel.MainDispatcherRule
import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CambiarContrasenaViewModelTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var viewModel: CambiarContrasenaViewModel

    @Before
    fun preparar() {
        val usuarios = UsuariosRepositoryEnMemoria()
        runBlocking { usuarios.registrar("Camila Rojas", "camila", "Segura#2026") }
        viewModel = CambiarContrasenaViewModel(usuarios, SesionEnMemoria("camila"))
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun llenarFormularioValido() {
        viewModel.onActualChange("Segura#2026")
        viewModel.onNuevaChange("Nueva#2027x")
        viewModel.onConfirmarChange("Nueva#2027x")
    }

    @Test
    fun cambiar_conContrasenaActualIncorrecta_muestraError() {
        llenarFormularioValido()
        viewModel.onActualChange("Equivocada#1")   // rompemos SOLO la contraseña actual

        viewModel.cambiar()

        assertEquals("La contraseña actual no es correcta.", viewModel.uiState.value.errorActual)
        assertFalse(viewModel.uiState.value.cambiada)
    }
}
