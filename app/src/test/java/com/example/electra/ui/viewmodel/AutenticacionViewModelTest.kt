package com.example.electra.ui.viewmodel

import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AutenticacionViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val usuarios = UsuariosRepositoryEnMemoria()
    private val sesion = SesionEnMemoria()

    @Test
    fun login_noMuestraErroresAntesDelPrimerIntento() {
        val vm = LoginViewModel(usuarios, sesion)

        assertNull(vm.uiState.value.errorUsuario)
        assertNull(vm.uiState.value.errorContrasena)
    }

    @Test
    fun login_camposVaciosMarcanErrorPorCampoSinConsultar() {
        val vm = LoginViewModel(usuarios, sesion)
        vm.onUsuarioChange("   ")

        vm.iniciarSesion()

        assertEquals("Ingresa tu usuario.", vm.uiState.value.errorUsuario)
        assertEquals("Ingresa tu contraseña.", vm.uiState.value.errorContrasena)
        assertEquals(1, vm.uiState.value.intentosFallidos)
        assertFalse(vm.uiState.value.credencialesInvalidas)
        assertFalse(vm.uiState.value.ingresoExitoso)
    }

    @Test
    fun login_credencialesIncorrectasMarcaError() = runTest {
        usuarios.registrar("Ana", "anatest", "Segura#2026")
        val vm = LoginViewModel(usuarios, sesion)
        vm.onUsuarioChange("anatest")
        vm.onContrasenaChange("otra")

        vm.iniciarSesion()

        assertTrue(vm.uiState.value.credencialesInvalidas)
        assertFalse(vm.uiState.value.ingresoExitoso)
        assertNull(sesion.usuarioActual())

        vm.onContrasenaChange("Segura#2026")
        assertFalse(vm.uiState.value.credencialesInvalidas)
    }

    @Test
    fun login_credencialesCorrectasIngresaGuardaSesionYSeConsume() = runTest {
        usuarios.registrar("Ana", "anatest", "Segura#2026")
        val vm = LoginViewModel(usuarios, sesion)
        vm.onUsuarioChange("  anatest ")
        vm.onContrasenaChange("Segura#2026")

        vm.iniciarSesion()

        assertTrue(vm.uiState.value.ingresoExitoso)
        assertEquals("anatest", sesion.usuarioActual())

        vm.ingresoConsumido()
        assertFalse(vm.uiState.value.ingresoExitoso)
    }

    private fun RegistroViewModel.completar(
        nombre: String = "Ana Test",
        usuario: String = "anatest",
        contrasena: String = "Segura#2026",
        confirmar: String = contrasena
    ) {
        onNombreChange(nombre)
        onUsuarioChange(usuario)
        onContrasenaChange(contrasena)
        onConfirmarChange(confirmar)
    }

    @Test
    fun registro_exitosoGuardaElUsuario() = runTest {
        val vm = RegistroViewModel(usuarios)
        vm.completar(usuario = " anatest ")

        vm.registrar()

        assertTrue(vm.uiState.value.registroExitoso)
        assertTrue(usuarios.validar("anatest", "Segura#2026"))
        assertEquals("Ana Test", usuarios.nombre("anatest"))
    }

    @Test
    fun registro_usuarioDuplicadoMarcaElCampoUsuario() = runTest {
        usuarios.registrar("Otro", "anatest", "Otra#2026x")
        val vm = RegistroViewModel(usuarios)
        vm.completar()

        vm.registrar()

        assertEquals("Ese usuario ya existe.", vm.uiState.value.errorUsuario)
        assertFalse(vm.uiState.value.registroExitoso)

        vm.onUsuarioChange("anatest2")
        assertNull(vm.uiState.value.errorUsuario)
    }

    @Test
    fun registro_formularioInvalidoMuestraErroresPorCampoYNoGuarda() = runTest {
        val vm = RegistroViewModel(usuarios)
        vm.completar(nombre = "", usuario = "an a", contrasena = "corta", confirmar = "otra")

        vm.registrar()

        val estado = vm.uiState.value
        assertNotNull(estado.errorNombre)
        assertEquals("El usuario no debe contener espacios.", estado.errorUsuario)
        assertEquals("La clave debe tener al menos 8 caracteres.", estado.errorContrasena)
        assertEquals("Las contraseñas no coinciden.", estado.errorConfirmar)
        assertEquals(1, estado.intentosFallidos)
        assertFalse(usuarios.validar("an a", "corta"))
    }

    @Test
    fun registro_requisitosSeActualizanMientrasSeEscribe() {
        val vm = RegistroViewModel(usuarios)

        vm.onContrasenaChange("abc")
        assertEquals(1, vm.uiState.value.requisitos.count { it.second })

        vm.onContrasenaChange("Segura#2026")
        assertTrue(vm.uiState.value.requisitos.all { it.second })
    }
}
