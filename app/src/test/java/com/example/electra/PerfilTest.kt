package com.example.electra

import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.data.ProtocolosRepositoryEnMemoria
import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.data.remote.ArchivosRemoto
import com.example.electra.data.remote.Conexion
import com.example.electra.data.remote.ConfiguracionApi
import com.example.electra.data.remote.RemotoUsuariosRepository
import com.example.electra.model.Avatar
import com.example.electra.model.ModoTema
import com.example.electra.model.Rol
import com.example.electra.ui.viewmodel.CambiarContrasenaViewModel
import com.example.electra.ui.viewmodel.EditarPerfilViewModel
import com.example.electra.ui.viewmodel.MensajeUsuarios
import com.example.electra.ui.viewmodel.PerfilViewModel
import com.example.electra.ui.viewmodel.UsuariosViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * PERFIL Y USUARIOS: cambiar contraseña, editar nombre/avatar/foto, ver el perfil, cerrar sesión
 * y la gestión de roles que hace el administrador.
 * Las pruebas "remoto_" usan RemotoUsuariosRepository con usuarios-service y archivos-service falsos.
 */
class PerfilTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var usuarios: UsuariosRepositoryEnMemoria

    // Para las pruebas contra el servidor falso
    private lateinit var api: UsuariosApiFalsa
    private lateinit var archivosApi: ArchivosApiFalsa
    private lateinit var conexion: Conexion
    private val fotosLocalesBorradas = mutableListOf<String>()
    private lateinit var remoto: RemotoUsuariosRepository

    @Before
    fun preparar() {
        usuarios = UsuariosRepositoryEnMemoria()
        runBlocking {
            usuarios.registrar("Camila Rojas", "camila", "Segura#2026")
            usuarios.registrar("Admin Electra", "admin", "Admin#2026x", Rol.ADMINISTRADOR)
        }

        api = UsuariosApiFalsa()
        api.agregar("camila", "Camila Rojas", "Segura#2026")
        archivosApi = ArchivosApiFalsa()
        conexion = Conexion()
        // leerLocal simula leer la foto del teléfono: solo existen las Uri "content://..."
        val archivos = ArchivosRemoto(archivosApi, conexion) { uri -> if (uri.startsWith("content://")) byteArrayOf(1, 2, 3) else null }
        remoto = RemotoUsuariosRepository(api, conexion, archivos, eliminarLocal = { fotosLocalesBorradas += it })
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun cambiarContrasena(actual: String, nueva: String) =
        CambiarContrasenaViewModel(usuarios, SesionEnMemoria("camila")).apply {
            onActualChange(actual)
            onNuevaChange(nueva)
            onConfirmarChange(nueva)
            cambiar()
        }

    // ---------------------------------------------------------------- contraseña

    @Test
    fun cambiar_conContrasenaActualIncorrecta_muestraError() {
        val viewModel = cambiarContrasena(actual = "Equivocada#1", nueva = "Nueva#2027x")

        assertEquals("La contraseña actual no es correcta.", viewModel.uiState.value.errorActual)
        assertFalse(viewModel.uiState.value.cambiada)
    }

    @Test
    fun cambiar_conDatosCorrectos_guardaLaNuevaContrasena() = runBlocking {
        val viewModel = cambiarContrasena(actual = "Segura#2026", nueva = "Nueva#2027x")

        assertTrue(viewModel.uiState.value.cambiada)
        assertTrue(usuarios.validar("camila", "Nueva#2027x"))
    }

    @Test
    fun cambiar_aLaMismaContrasena_esRechazado() {
        val viewModel = cambiarContrasena(actual = "Segura#2026", nueva = "Segura#2026")

        assertEquals("La nueva contraseña debe ser distinta de la actual.", viewModel.uiState.value.errorNueva)
    }

    // ---------------------------------------------------------------- editar perfil

    @Test
    fun guardar_conNombreNuevo_actualizaElUsuario() = runBlocking {
        val viewModel = EditarPerfilViewModel(usuarios, SesionEnMemoria("camila"))
        viewModel.onNombreChange("Camila Andrea Rojas")
        viewModel.onColorChange("verde")

        viewModel.guardar()

        assertTrue(viewModel.uiState.value.guardado)
        assertEquals("Camila Andrea Rojas", usuarios.nombre("camila"))
        assertEquals("verde", usuarios.avatar("camila").color)
    }

    @Test
    fun guardar_conNombreInvalido_noGuarda() = runBlocking {
        val viewModel = EditarPerfilViewModel(usuarios, SesionEnMemoria("camila"))
        viewModel.onNombreChange("C4")

        viewModel.guardar()

        assertEquals("El nombre debe tener al menos 3 letras.", viewModel.uiState.value.errorNombre)
        assertEquals("Camila Rojas", usuarios.nombre("camila"))
    }

    @Test
    fun cambiarFoto_variasVeces_borraLasFotosQueNoSeGuardaronYLaAnterior() = runBlocking {
        usuarios.actualizarAvatar("camila", Avatar(foto = "content://vieja"))
        val borradas = mutableListOf<String>()
        val viewModel = EditarPerfilViewModel(usuarios, SesionEnMemoria("camila")) { borradas += it }

        viewModel.onFotoElegida("content://primera")
        viewModel.onFotoElegida("content://segunda") // la primera ya no sirve
        viewModel.guardar()                            // al guardar, la vieja tampoco

        assertEquals(listOf("content://primera", "content://vieja"), borradas)
        assertEquals("content://segunda", usuarios.avatar("camila").foto)
    }

    // ---------------------------------------------------------------- ver perfil y gestión de usuarios

    @Test
    fun perfil_cargar_muestraDatosYConteos_yCerrarSesionLaTermina() {
        val sesion = SesionEnMemoria("camila")
        val viewModel = PerfilViewModel(
            usuarios, sesion,
            PruebasRepositoryEnMemoria(listOf(prueba(1), prueba(2))),
            ProtocolosRepositoryEnMemoria(listOf(protocolo(1, "#000001"), protocolo(2, "#000002", estado = "Formalizado")))
        )

        viewModel.cargar()
        assertEquals("Camila Rojas", viewModel.uiState.value.nombre)
        assertEquals(Rol.TECNICO, viewModel.uiState.value.rol)
        assertEquals(2, viewModel.uiState.value.pruebasGuardadas)
        assertEquals(1, viewModel.uiState.value.protocolosPendientes)

        viewModel.cerrarSesion()
        assertTrue(viewModel.uiState.value.sesionCerrada)
        assertNull(sesion.usuarioActual())
    }

    @Test
    fun administrador_cambiaElRolDeOtroUsuario() = runBlocking {
        val viewModel = UsuariosViewModel(usuarios, SesionEnMemoria("admin"))
        viewModel.cargar()
        val camila = viewModel.uiState.value.usuarios.first { it.usuario == "camila" }

        viewModel.abrirCambioRol(camila)
        viewModel.onRolElegido(Rol.SUPERVISOR)
        viewModel.confirmarCambioRol()

        assertEquals(MensajeUsuarios.ROL_ACTUALIZADO, viewModel.uiState.value.mensaje)
        assertEquals(Rol.SUPERVISOR, usuarios.rol("camila"))
        assertEquals(1, viewModel.uiState.value.conteoPorRol[Rol.SUPERVISOR])
    }

    @Test
    fun administrador_noPuedeCambiarSuPropioRol() {
        val viewModel = UsuariosViewModel(usuarios, SesionEnMemoria("admin"))
        viewModel.cargar()
        val propio = viewModel.uiState.value.usuarios.first { it.usuario == "admin" }

        viewModel.abrirCambioRol(propio)

        assertEquals(MensajeUsuarios.PROPIO_ROL, viewModel.uiState.value.mensaje)
        assertNull(viewModel.uiState.value.editando)
    }

    @Test
    fun tecnico_noVeLaListaDeUsuarios() {
        val viewModel = UsuariosViewModel(usuarios, SesionEnMemoria("camila"))

        viewModel.cargar()

        assertTrue(viewModel.uiState.value.usuarios.isEmpty())
        assertFalse(viewModel.uiState.value.rolActual!!.puedeGestionarUsuarios)
    }

    // ---------------------------------------------------------------- modelos del perfil

    @Test
    fun iniciales_seFormanConElPrimerYUltimoNombre() {
        assertEquals("AT", Avatar.iniciales("Ana María Torres"))
        assertEquals("A", Avatar.iniciales("ana"))
        assertEquals("?", Avatar.iniciales("  "))
    }

    @Test
    fun modoTema_decideSiUsarLaPaletaOscura() {
        assertTrue(ModoTema.SISTEMA.esOscuro(sistemaOscuro = true))
        assertFalse(ModoTema.CLARO.esOscuro(sistemaOscuro = true))
        assertTrue(ModoTema.OSCURO.esOscuro(sistemaOscuro = false))
        assertEquals(ModoTema.OSCURO, ModoTema.desdeClave("oscuro"))
        assertEquals(ModoTema.SISTEMA, ModoTema.desdeClave("desconocido"))
    }

    @Test
    fun roles_separanQuienRegistraDeQuienFormaliza() {
        assertTrue(Rol.TECNICO.puedeRegistrarPruebas)
        assertFalse(Rol.TECNICO.puedeFormalizar)
        assertFalse(Rol.SUPERVISOR.puedeRegistrarPruebas)
        assertTrue(Rol.SUPERVISOR.veRegistrosDeTodos)
        assertEquals(Rol.ADMINISTRADOR, Rol.desdeClave("admin"))
        assertEquals(4, Rol.ADMINISTRADOR.permisos.count { it.second })
    }

    // ---------------------------------------------------------------- usuarios-service y archivos-service (falsos)

    @Test
    fun remoto_actualizarAvatar_subeLaFotoLocalYGuardaSoloSuRuta() = runBlocking {
        val ok = remoto.actualizarAvatar("camila", Avatar(color = "verde", foto = "content://perfil.jpg"))

        assertTrue(ok)
        assertEquals(1, archivosApi.subidas)
        assertEquals("/api/archivos/foto1", api.cuentas.getValue("camila").avatar?.foto) // ruta relativa en el servidor
        assertEquals(listOf("content://perfil.jpg"), fotosLocalesBorradas)               // la copia local sobra
        assertEquals(ConfiguracionApi.urlCompleta("/api/archivos/foto1"), remoto.avatar("camila").foto) // URL para Coil
    }

    @Test
    fun remoto_actualizarAvatar_siFallaLaSubidaNoCambiaNada() = runBlocking {
        archivosApi.caido = true

        assertFalse(remoto.actualizarAvatar("camila", Avatar(foto = "content://perfil.jpg")))
        assertNull(api.cuentas.getValue("camila").avatar?.foto)
        assertTrue(fotosLocalesBorradas.isEmpty()) // la foto local se conserva para reintentar
    }

    @Test
    fun remoto_avatarYaSubido_noSeVuelveASubir() = runBlocking {
        val url = ConfiguracionApi.urlCompleta("/api/archivos/existente")

        assertTrue(remoto.actualizarAvatar("camila", Avatar(foto = url)))
        assertEquals(0, archivosApi.subidas)
        assertEquals("/api/archivos/existente", api.cuentas.getValue("camila").avatar?.foto)
    }

    @Test
    fun remoto_nombreRolYListado_seActualizanEnElServidor() = runBlocking {
        assertTrue(remoto.actualizarNombre("camila", " Camila A. Rojas "))
        assertTrue(remoto.actualizarRol("camila", Rol.ADMINISTRADOR))
        assertFalse(remoto.actualizarRol("nadie", Rol.ADMINISTRADOR)) // 404

        val lista = remoto.listar()
        assertEquals("Camila A. Rojas", lista.single().nombre)
        assertEquals(Rol.ADMINISTRADOR, lista.single().rol)
    }
}
