package com.example.electra.ui.viewmodel

import com.example.electra.data.UsuariosRepositoryEnMemoria
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AutenticacionViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val usuarios = UsuariosRepositoryEnMemoria()

    // --- LOGIN ---

    @Test
    fun login_usuarioVacioMarcaErrorSinConsultar() {
        val vm = LoginViewModel(usuarios)
        vm.iniciarSesion("   ", "Segura#2026")

        assertTrue(vm.uiState.value.error)
        assertFalse(vm.uiState.value.ingresoExitoso)
    }

    @Test
    fun login_credencialesIncorrectasMarcaError() = runTest {
        usuarios.registrar("Ana", "anatest", "Segura#2026")
        val vm = LoginViewModel(usuarios)

        vm.iniciarSesion("anatest", "otra")

        assertTrue(vm.uiState.value.error)
        assertFalse(vm.uiState.value.ingresoExitoso)
    }

    @Test
    fun login_credencialesCorrectasIngresaYSeConsume() = runTest {
        usuarios.registrar("Ana", "anatest", "Segura#2026")
        val vm = LoginViewModel(usuarios)

        vm.iniciarSesion("  anatest ", "Segura#2026")
        assertTrue(vm.uiState.value.ingresoExitoso)
        assertFalse(vm.uiState.value.error)

        vm.ingresoConsumido()
        assertFalse(vm.uiState.value.ingresoExitoso)
    }

    // --- REGISTRO ---

    @Test
    fun registro_exitosoGuardaElUsuario() = runTest {
        val vm = RegistroViewModel(usuarios)

        vm.registrar("Ana Test", " anatest ", "Segura#2026", "Segura#2026")

        assertTrue(vm.uiState.value.registroExitoso)
        assertEquals("", vm.uiState.value.error)
        assertTrue(usuarios.validar("anatest", "Segura#2026"))
    }

    @Test
    fun registro_usuarioDuplicadoMuestraError() = runTest {
        usuarios.registrar("Otro", "anatest", "Otra#2026x")
        val vm = RegistroViewModel(usuarios)

        vm.registrar("Ana Test", "anatest", "Segura#2026", "Segura#2026")

        assertEquals("Ese usuario ya existe.", vm.uiState.value.error)
        assertFalse(vm.uiState.value.registroExitoso)
    }

    @Test
    fun registro_formularioInvalidoNoLlamaAlRepositorio() = runTest {
        val vm = RegistroViewModel(usuarios)

        vm.registrar("Ana Test", "anatest", "corta", "corta")

        assertEquals("La clave debe tener al menos 8 caracteres.", vm.uiState.value.error)
        assertFalse(usuarios.validar("anatest", "corta"))
    }

    // --- VALIDACIONES DEL FORMULARIO ---

    @Test
    fun validarRegistro_reglas() {
        val ok = "Segura#2026"
        assertEquals("Completa todos los campos.", validarRegistro("", "u", ok, ok))
        assertEquals("El usuario no debe contener espacios.", validarRegistro("Ana", "ana test", ok, ok))
        assertEquals("La clave no puede superar los 20 caracteres.", validarRegistro("Ana", "anat", "Segura#2026Segura#2026", "x"))
        assertEquals("La clave debe incluir al menos una letra mayúscula.", validarRegistro("Ana", "anat", "segura#2026", "x"))
        assertEquals("La clave debe incluir al menos una letra minúscula.", validarRegistro("Ana", "anat", "SEGURA#2026", "x"))
        assertEquals("La clave debe incluir al menos un número.", validarRegistro("Ana", "anat", "Segura#clave", "x"))
        assertEquals("La clave debe incluir un carácter especial: ! @ # $ % & * ?", validarRegistro("Ana", "anat", "Segura2026", "x"))
        assertEquals("No incluyas el nombre de la aplicación.", validarRegistro("Ana", "anat", "Electra#2026", "x"))
        assertEquals("No incluyas tu nombre ni tu usuario.", validarRegistro("Marcela", "anat", "Marcela#2026", "x"))
        assertEquals("Las contraseñas no coinciden.", validarRegistro("Ana", "anat", ok, "Segura#2027"))
        assertNull(validarRegistro("Ana", "anat", ok, ok))
    }
}
