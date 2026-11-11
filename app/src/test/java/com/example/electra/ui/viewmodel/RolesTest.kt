package com.example.electra.ui.viewmodel

import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.data.rolActual
import com.example.electra.model.Rol
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RolesTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val usuarios = UsuariosRepositoryEnMemoria()
    private val sesion = SesionEnMemoria()

    @Test
    fun permisos_separacionDeFunciones() {
        assertTrue(Rol.TECNICO.puedeRegistrarPruebas)
        assertFalse(Rol.TECNICO.puedeFormalizar)
        assertFalse(Rol.SUPERVISOR.puedeRegistrarPruebas)
        assertFalse(Rol.SUPERVISOR.puedeCrearProtocolos)
        assertTrue(Rol.SUPERVISOR.puedeFormalizar)
        assertTrue(Rol.ADMINISTRADOR.puedeGestionarUsuarios)
        assertFalse(Rol.SUPERVISOR.puedeGestionarUsuarios)
    }

    @Test
    fun desdeClave_desconocidaOAusenteEsTecnico() {
        assertEquals(Rol.TECNICO, Rol.desdeClave(null))
        assertEquals(Rol.TECNICO, Rol.desdeClave("otro"))
        assertEquals(Rol.SUPERVISOR, Rol.desdeClave("supervisor"))
    }

    @Test
    fun registro_guardaElRolElegido() = runTest {
        val vm = RegistroViewModel(usuarios)
        vm.onNombreChange("Sara Rojas")
        vm.onUsuarioChange("srojas")
        vm.onContrasenaChange("Segura#2026")
        vm.onConfirmarChange("Segura#2026")
        vm.onRolChange(Rol.SUPERVISOR)

        vm.registrar()

        assertTrue(vm.uiState.value.registroExitoso)
        assertEquals(Rol.SUPERVISOR, usuarios.rol("srojas"))
    }

    @Test
    fun rolActual_sinSesionEsTecnico() = runTest {
        assertEquals(Rol.TECNICO, usuarios.rolActual(sesion))
    }

    @Test
    fun usuarios_adminCambiaElRolDeOtro() = runTest {
        usuarios.registrar("Admin", "admin", "Segura#2026", Rol.ADMINISTRADOR)
        usuarios.registrar("Tomás", "tomas", "Segura#2026", Rol.TECNICO)
        sesion.iniciar("admin")
        val vm = UsuariosViewModel(usuarios, sesion)
        vm.cargar()
        assertEquals(2, vm.uiState.value.usuarios.size)

        vm.abrirCambioRol(vm.uiState.value.usuarios.first { it.usuario == "tomas" })
        vm.onRolElegido(Rol.SUPERVISOR)
        vm.confirmarCambioRol()

        assertEquals(Rol.SUPERVISOR, usuarios.rol("tomas"))
        assertEquals(MensajeUsuarios.ROL_ACTUALIZADO, vm.uiState.value.mensaje)
        assertNull(vm.uiState.value.editando)
    }

    @Test
    fun usuarios_adminNoPuedeCambiarSuPropioRol() = runTest {
        usuarios.registrar("Admin", "admin", "Segura#2026", Rol.ADMINISTRADOR)
        sesion.iniciar("admin")
        val vm = UsuariosViewModel(usuarios, sesion)
        vm.cargar()

        vm.abrirCambioRol(vm.uiState.value.usuarios.first())

        assertNull(vm.uiState.value.editando)
        assertEquals(MensajeUsuarios.PROPIO_ROL, vm.uiState.value.mensaje)
        assertEquals(Rol.ADMINISTRADOR, usuarios.rol("admin"))
    }

    @Test
    fun usuarios_noAdminNoVeNiCambiaRoles() = runTest {
        usuarios.registrar("Tomás", "tomas", "Segura#2026", Rol.TECNICO)
        usuarios.registrar("Sara", "sara", "Segura#2026", Rol.TECNICO)
        sesion.iniciar("tomas")
        val vm = UsuariosViewModel(usuarios, sesion)
        vm.cargar()

        assertTrue(vm.uiState.value.usuarios.isEmpty())
        assertEquals(Rol.TECNICO, vm.uiState.value.rolActual)
    }
}
