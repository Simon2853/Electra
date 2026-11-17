package com.example.electra.ui.viewmodel.autenticacion

import com.example.electra.ui.viewmodel.RecuperarContrasenaViewModel
import com.example.electra.ui.viewmodel.MainDispatcherRule
import com.example.electra.data.UsuariosRepositoryEnMemoria
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RecuperarContrasenaViewModelTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var viewModel: RecuperarContrasenaViewModel
    private lateinit var usuarios: UsuariosRepositoryEnMemoria

    @Before
    fun preparar() {
        usuarios = UsuariosRepositoryEnMemoria()
        runBlocking { usuarios.registrar("Camila Rojas", "camila", "Segura#2026") }
        viewModel = RecuperarContrasenaViewModel(usuarios)
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun llenarFormularioValido() {
        viewModel.onUsuarioChange("camila")
        viewModel.onNombreChange("Camila Rojas")
        viewModel.onContrasenaChange("Nueva#2027x")
        viewModel.onConfirmarChange("Nueva#2027x")
    }

    @Test
    fun restablecer_conDatosCorrectos_cambiaLaContrasena() = runBlocking {
        llenarFormularioValido()

        viewModel.restablecer()

        assertTrue(viewModel.uiState.value.restablecida)
        assertTrue(usuarios.validar("camila", "Nueva#2027x"))
    }
}
