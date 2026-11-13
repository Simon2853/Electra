package com.example.electra.ui.viewmodel

import com.example.electra.data.Alcance
import com.example.electra.data.ProtocolosRepositoryEnMemoria
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.data.alcanceActual
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.Rol
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Cada prueba y protocolo guarda quién lo creó y cada usuario ve solo lo que le corresponde. */
class RegistrosPorUsuarioTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    /** Reloj compartido que avanza 1 ms en cada lectura: el orden de creación queda determinado. */
    private var ahora = 1_000L
    private val reloj: () -> Long = { ahora++ }

    private val usuarios = UsuariosRepositoryEnMemoria(reloj)
    private val sesion = SesionEnMemoria()
    private val alcance: suspend () -> Alcance = { alcanceActual(sesion, usuarios) }

    private val pruebas = PruebasRepositoryEnMemoria(alcance = alcance, reloj = reloj)
    private val protocolos = ProtocolosRepositoryEnMemoria(alcance = alcance, reloj = reloj)

    private fun conSesion(usuario: String) = sesion.iniciar(usuario)

    private suspend fun registrarUsuarios() {
        usuarios.registrar("Ana Técnica", "ana", "clave1", Rol.TECNICO)
        usuarios.registrar("Beto Técnico", "beto", "clave1", Rol.TECNICO)
        usuarios.registrar("Sara Supervisora", "sara", "clave1", Rol.SUPERVISOR)
        usuarios.registrar("Admin", "admin", "clave1", Rol.ADMINISTRADOR)
    }

    @Test
    fun insertar_guardaComoCreadorAlUsuarioDeLaSesion() = runTest {
        registrarUsuarios()
        conSesion("ana")

        val id = pruebas.insertar(prueba())
        protocolos.insertar(protocolo(0, "#000001"))

        assertEquals("ana", pruebas.pruebas.single { it.id == id }.creadoPor)
        assertEquals("ana", protocolos.protocolos.single().creadoPor)
    }

    @Test
    fun tecnico_soloVeSusPropiosRegistros() = runTest {
        registrarUsuarios()
        conSesion("ana")
        val idAna = pruebas.insertar(prueba(equipo = "TR-001"))
        protocolos.insertar(protocolo(0, "#000001"))

        conSesion("beto")
        val idBeto = pruebas.insertar(prueba(equipo = "RL-004"))
        protocolos.insertar(protocolo(0, "#000002"))

        assertEquals(listOf(idBeto), pruebas.listarTodas().map { it.id })
        assertEquals(listOf("RL-004"), pruebas.equiposConRegistros())
        assertTrue(pruebas.listarPorEquipo("TR-001").isEmpty())
        assertNull(pruebas.buscarPorId(idAna))
        assertEquals(listOf("#000002"), protocolos.listar().map { it.numero })
        assertNull(protocolos.buscar("000001"))
    }

    @Test
    fun tecnico_noPuedeModificarProtocolosAjenos() = runTest {
        registrarUsuarios()
        conSesion("ana")
        protocolos.insertar(protocolo(0, "#000001"))

        conSesion("beto")
        assertFalse(protocolos.actualizarObservaciones("000001", "Cambio ajeno"))
        assertFalse(protocolos.cambiarEstado("000001", "Formalizado"))
        assertEquals("", protocolos.protocolos.single().observaciones)
    }

    @Test
    fun supervisorYAdministrador_venLosRegistrosDeTodos() = runTest {
        registrarUsuarios()
        conSesion("ana")
        pruebas.insertar(prueba())
        protocolos.insertar(protocolo(0, "#000001"))
        conSesion("beto")
        pruebas.insertar(prueba())
        protocolos.insertar(protocolo(0, "#000002"))

        for (usuario in listOf("sara", "admin")) {
            conSesion(usuario)
            assertEquals(2, pruebas.listarTodas().size)
            assertEquals(2, protocolos.listar().size)
        }
        conSesion("sara")
        assertTrue(protocolos.cambiarEstado("000001", "Formalizado"))
    }

    @Test
    fun usuarioNuevo_parteSinRegistros() = runTest {
        registrarUsuarios()
        conSesion("ana")
        pruebas.insertar(prueba())
        protocolos.insertar(protocolo(0, "#000001"))

        usuarios.registrar("Nuevo", "nuevo", "clave1", Rol.TECNICO)
        conSesion("nuevo")

        assertTrue(pruebas.listarTodas().isEmpty())
        assertTrue(protocolos.listar().isEmpty())
        assertEquals(CatalogoEquipos.CODIGOS_POR_DEFECTO, pruebas.equiposConRegistros())

        val home = HomeViewModel(pruebas, protocolos, usuarios, sesion).apply { cargar() }
        assertEquals(0, home.uiState.value.totalPruebas)
        assertEquals(0, home.uiState.value.totalProtocolos)
        assertTrue(home.uiState.value.recientes.isEmpty())
    }

    @Test
    fun cuentaNueva_parteVaciaSinImportarElRol() = runTest {
        registrarUsuarios()
        conSesion("ana")
        pruebas.insertar(prueba())
        protocolos.insertar(protocolo(0, "#000001"))

        for (rol in Rol.entries) {
            val usuario = "nuevo_${rol.clave}"
            usuarios.registrar("Nuevo", usuario, "clave1", rol)
            conSesion(usuario)

            assertTrue("$rol no debe ver pruebas previas", pruebas.listarTodas().isEmpty())
            assertTrue("$rol no debe ver protocolos previos", protocolos.listar().isEmpty())
            assertNull(protocolos.buscar("000001"))
            assertFalse(protocolos.cambiarEstado("000001", "Formalizado"))

            val home = HomeViewModel(pruebas, protocolos, usuarios, sesion).apply { cargar() }
            assertEquals(0, home.uiState.value.totalPruebas)
            assertEquals(0, home.uiState.value.totalProtocolos)
        }
    }

    @Test
    fun supervisorNuevo_veLoQueLosTecnicosCreanDespues() = runTest {
        registrarUsuarios()
        conSesion("ana")
        protocolos.insertar(protocolo(0, "#000001"))

        usuarios.registrar("Nuevo Supervisor", "super2", "clave1", Rol.SUPERVISOR)

        conSesion("ana")
        protocolos.insertar(protocolo(0, "#000002"))

        conSesion("super2")
        assertEquals(listOf("#000002"), protocolos.listar().map { it.numero })
        assertTrue(protocolos.cambiarEstado("000002", "Formalizado"))
    }

    @Test
    fun cuentaAnteriorALaRegla_veTodo() {
        // Cuentas creadas antes de guardar la fecha de creación (desde = null) siguen viendo todo.
        val antigua = Alcance(usuario = "jefe", veTodo = true, desde = null)
        assertTrue(antigua.puedeVer(creadoPor = null, creadoEn = null))
        assertTrue(antigua.puedeVer(creadoPor = "ana", creadoEn = 5L))

        val nueva = Alcance(usuario = "jefe", veTodo = true, desde = 10L)
        assertFalse(nueva.puedeVer(creadoPor = null, creadoEn = null))
        assertFalse(nueva.puedeVer(creadoPor = "ana", creadoEn = 9L))
        assertTrue(nueva.puedeVer(creadoPor = "ana", creadoEn = 10L))
    }

    @Test
    fun sinSesion_noSeVeNada() = runTest {
        registrarUsuarios()
        conSesion("ana")
        pruebas.insertar(prueba())
        sesion.cerrar()

        assertTrue(pruebas.listarTodas().isEmpty())
    }

    @Test
    fun siguienteNumero_cuentaLosProtocolosDeTodos() = runTest {
        registrarUsuarios()
        conSesion("ana")
        protocolos.insertar(protocolo(0, "#000007"))

        conSesion("beto")
        assertEquals("#000008", protocolos.siguienteNumero())
        assertFalse("El número es único entre usuarios", protocolos.insertar(protocolo(0, "#000007")))
    }
}
