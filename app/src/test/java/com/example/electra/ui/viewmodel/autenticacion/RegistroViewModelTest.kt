package com.example.electra.ui.viewmodel.autenticacion

import com.example.electra.ui.viewmodel.RegistroViewModel
import com.example.electra.ui.viewmodel.MainDispatcherRule
import com.example.electra.data.UsuariosRepositoryEnMemoria
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegistroViewModelTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var viewModel: RegistroViewModel
    private lateinit var usuarios: UsuariosRepositoryEnMemoria

    @Before
    fun preparar() {
        usuarios = UsuariosRepositoryEnMemoria()
        viewModel = RegistroViewModel(usuarios)
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun llenarFormularioValido() {
        viewModel.onNombreChange("Camila Rojas")
        viewModel.onUsuarioChange("camila")
        viewModel.onContrasenaChange("Segura#2026")
        viewModel.onConfirmarChange("Segura#2026")
    }

    @Test
    fun registrar_conDatosValidos_creaElUsuario() = runBlocking {
        llenarFormularioValido()

        viewModel.registrar()

        assertTrue(viewModel.uiState.value.registroExitoso)
        assertEquals("Camila Rojas", usuarios.nombre("camila"))
    }
}
