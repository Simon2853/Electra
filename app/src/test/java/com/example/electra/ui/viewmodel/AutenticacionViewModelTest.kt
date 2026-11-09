package com.example.electra.ui.viewmodel

import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.model.Avatar
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

    private fun RecuperarContrasenaViewModel.completar(
        usuario: String = "anatest",
        nombre: String = "Ana Test",
        contrasena: String = "Nueva#2027x",
        confirmar: String = contrasena
    ) {
        onUsuarioChange(usuario)
        onNombreChange(nombre)
        onContrasenaChange(contrasena)
        onConfirmarChange(confirmar)
    }

    @Test
    fun recuperar_datosCorrectosCambiaLaContrasena() = runTest {
        usuarios.registrar("Ana Test", "anatest", "Segura#2026")
        val vm = RecuperarContrasenaViewModel(usuarios)
        vm.completar(nombre = " ana test ")

        vm.restablecer()

        assertTrue(vm.uiState.value.restablecida)
        assertTrue(usuarios.validar("anatest", "Nueva#2027x"))
        assertFalse(usuarios.validar("anatest", "Segura#2026"))

        vm.restablecidaConsumida()
        assertFalse(vm.uiState.value.restablecida)
    }

    @Test
    fun recuperar_nombreQueNoCoincideNoCambiaNada() = runTest {
        usuarios.registrar("Ana Test", "anatest", "Segura#2026")
        val vm = RecuperarContrasenaViewModel(usuarios)
        vm.completar(nombre = "Otra Persona")

        vm.restablecer()

        assertTrue(vm.uiState.value.datosNoCoinciden)
        assertFalse(vm.uiState.value.restablecida)
        assertEquals(1, vm.uiState.value.intentosFallidos)
        assertTrue(usuarios.validar("anatest", "Segura#2026"))

        vm.onNombreChange("Ana Test")
        assertFalse(vm.uiState.value.datosNoCoinciden)
    }

    @Test
    fun recuperar_usuarioInexistenteMarcaDatosNoCoinciden() = runTest {
        val vm = RecuperarContrasenaViewModel(usuarios)
        vm.completar(usuario = "nadie")

        vm.restablecer()

        assertTrue(vm.uiState.value.datosNoCoinciden)
        assertFalse(vm.uiState.value.restablecida)
    }

    @Test
    fun recuperar_formularioInvalidoMuestraErroresYNoConsulta() = runTest {
        usuarios.registrar("Ana Test", "anatest", "Segura#2026")
        val vm = RecuperarContrasenaViewModel(usuarios)
        vm.completar(usuario = "", nombre = "", contrasena = "corta", confirmar = "otra")

        vm.restablecer()

        val estado = vm.uiState.value
        assertEquals("Ingresa tu usuario.", estado.errorUsuario)
        assertNotNull(estado.errorNombre)
        assertEquals("La clave debe tener al menos 8 caracteres.", estado.errorContrasena)
        assertEquals("Las contraseñas no coinciden.", estado.errorConfirmar)
        assertFalse(estado.datosNoCoinciden)
        assertTrue(usuarios.validar("anatest", "Segura#2026"))
    }

    @Test
    fun editarPerfil_cargaElNombreYGuardaElNuevo() = runTest {
        usuarios.registrar("Ana Test", "anatest", "Segura#2026")
        sesion.iniciar("anatest")
        val vm = EditarPerfilViewModel(usuarios, sesion)
        assertEquals("Ana Test", vm.uiState.value.nombre)
        assertEquals("anatest", vm.uiState.value.usuario)

        vm.onNombreChange("  Ana María Test ")
        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertEquals("Ana María Test", usuarios.nombre("anatest"))
        assertTrue(usuarios.validar("anatest", "Segura#2026"))
    }

    @Test
    fun editarPerfil_guardaIconoYColorDelAvatar() = runTest {
        usuarios.registrar("Ana Test", "anatest", "Segura#2026")
        sesion.iniciar("anatest")
        val vm = EditarPerfilViewModel(usuarios, sesion)
        assertEquals(Avatar(), vm.uiState.value.avatar)

        vm.onIconoChange("ubicacion")
        vm.onColorChange("verde")
        vm.onFondoChange("lavanda")
        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertEquals(Avatar(icono = "ubicacion", color = "verde", fondo = "lavanda"), usuarios.avatar("anatest"))
    }

    @Test
    fun editarPerfil_nombreInvalidoNoGuarda() = runTest {
        usuarios.registrar("Ana Test", "anatest", "Segura#2026")
        sesion.iniciar("anatest")
        val vm = EditarPerfilViewModel(usuarios, sesion)

        vm.onNombreChange("A1")
        vm.guardar()

        assertNotNull(vm.uiState.value.errorNombre)
        assertEquals(1, vm.uiState.value.intentosFallidos)
        assertFalse(vm.uiState.value.guardado)
        assertEquals("Ana Test", usuarios.nombre("anatest"))
    }

    private fun CambiarContrasenaViewModel.completar(
        actual: String = "Segura#2026",
        nueva: String = "Nueva#2027x",
        confirmar: String = nueva
    ) {
        onActualChange(actual)
        onNuevaChange(nueva)
        onConfirmarChange(confirmar)
    }

    private suspend fun sesionDeAna() {
        usuarios.registrar("Ana Test", "anatest", "Segura#2026")
        sesion.iniciar("anatest")
    }

    @Test
    fun cambiarContrasena_conActualCorrectaLaCambia() = runTest {
        sesionDeAna()
        val vm = CambiarContrasenaViewModel(usuarios, sesion)
        vm.completar()

        vm.cambiar()

        assertTrue(vm.uiState.value.cambiada)
        assertTrue(usuarios.validar("anatest", "Nueva#2027x"))
        assertFalse(usuarios.validar("anatest", "Segura#2026"))
    }

    @Test
    fun cambiarContrasena_conActualIncorrectaMarcaElCampo() = runTest {
        sesionDeAna()
        val vm = CambiarContrasenaViewModel(usuarios, sesion)
        vm.completar(actual = "Equivocada#1")

        vm.cambiar()

        assertEquals("La contraseña actual no es correcta.", vm.uiState.value.errorActual)
        assertFalse(vm.uiState.value.cambiada)
        assertTrue(usuarios.validar("anatest", "Segura#2026"))

        vm.onActualChange("Segura#2026")
        assertNull(vm.uiState.value.errorActual)
    }

    @Test
    fun cambiarContrasena_nuevaIgualALaActualSeRechaza() = runTest {
        sesionDeAna()
        val vm = CambiarContrasenaViewModel(usuarios, sesion)
        vm.completar(nueva = "Segura#2026")

        vm.cambiar()

        assertEquals("La nueva contraseña debe ser distinta de la actual.", vm.uiState.value.errorNueva)
        assertFalse(vm.uiState.value.cambiada)
    }

    @Test
    fun cambiarContrasena_nuevaConElNombreDelUsuarioSeRechaza() = runTest {
        sesionDeAna()
        val vm = CambiarContrasenaViewModel(usuarios, sesion)
        vm.completar(nueva = "Anatest#2027")

        vm.cambiar()

        assertEquals("No incluyas tu nombre ni tu usuario.", vm.uiState.value.errorNueva)
        assertFalse(vm.uiState.value.cambiada)
    }
}
