package com.example.electra.ui.viewmodel.perfil

import com.example.electra.ui.viewmodel.EditarPerfilViewModel
import com.example.electra.ui.viewmodel.MainDispatcherRule
import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EditarPerfilViewModelTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var viewModel: EditarPerfilViewModel
    private lateinit var usuarios: UsuariosRepositoryEnMemoria

    @Before
    fun preparar() {
        usuarios = UsuariosRepositoryEnMemoria()
        runBlocking { usuarios.registrar("Camila Rojas", "camila", "Segura#2026") }
        viewModel = EditarPerfilViewModel(usuarios, SesionEnMemoria("camila"))
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun cambiarNombre() {
        viewModel.onNombreChange("Camila Andrea Rojas")
    }

    @Test
    fun guardar_conNombreNuevo_actualizaElUsuario() = runBlocking {
        cambiarNombre()

        viewModel.guardar()

        assertTrue(viewModel.uiState.value.guardado)
        assertEquals("Camila Andrea Rojas", usuarios.nombre("camila"))
    }
}
