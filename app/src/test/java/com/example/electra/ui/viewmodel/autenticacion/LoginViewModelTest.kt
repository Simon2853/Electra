package com.example.electra.ui.viewmodel.autenticacion

import com.example.electra.ui.viewmodel.LoginViewModel
import com.example.electra.ui.viewmodel.MainDispatcherRule
import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var viewModel: LoginViewModel
    private lateinit var sesion: SesionEnMemoria

    @Before
    fun preparar() {
        val usuarios = UsuariosRepositoryEnMemoria()
        runBlocking { usuarios.registrar("Camila Rojas", "camila", "Segura#2026") }
        sesion = SesionEnMemoria()
        viewModel = LoginViewModel(usuarios, sesion)
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun llenarCredencialesValidas() {
        viewModel.onUsuarioChange("camila")
        viewModel.onContrasenaChange("Segura#2026")
    }

    @Test
    fun iniciarSesion_conCredencialesCorrectas_guardaLaSesion() {
        llenarCredencialesValidas()

        viewModel.iniciarSesion()

        assertTrue(viewModel.uiState.value.ingresoExitoso)
        assertEquals("camila", sesion.usuarioActual())
    }
}
