package com.example.electra

import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.data.remote.ArchivosRemoto
import com.example.electra.data.remote.Conexion
import com.example.electra.data.remote.RemotoUsuariosRepository
import com.example.electra.model.Rol
import com.example.electra.ui.viewmodel.LoginViewModel
import com.example.electra.ui.viewmodel.RecuperarContrasenaViewModel
import com.example.electra.ui.viewmodel.RegistroViewModel
import com.example.electra.validation.Validaciones
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * AUTENTICACIÓN: iniciar sesión, crear cuenta y recuperar la contraseña.
 * - Pruebas de ViewModel: usan el repositorio en memoria (sin servidor).
 * - Pruebas "remoto_": usan RemotoUsuariosRepository contra un usuarios-service falso (UsuariosApiFalsa).
 */
class AutenticacionTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var usuarios: UsuariosRepositoryEnMemoria
    private lateinit var sesion: SesionEnMemoria

    // Para las pruebas contra el servidor falso
    private lateinit var api: UsuariosApiFalsa
    private lateinit var conexion: Conexion
    private lateinit var remoto: RemotoUsuariosRepository

    @Before
    fun preparar() {
        usuarios = UsuariosRepositoryEnMemoria()
        runBlocking { usuarios.registrar("Camila Rojas", "camila", "Segura#2026") }
        sesion = SesionEnMemoria()

        api = UsuariosApiFalsa()
        api.agregar("camila", "Camila Rojas", "Segura#2026", rol = "supervisor", creadaEn = 777)
        conexion = Conexion()
        remoto = RemotoUsuariosRepository(api, conexion, ArchivosRemoto(ArchivosApiFalsa(), conexion) { null })
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun loginCon(usuario: String, contrasena: String): LoginViewModel =
        LoginViewModel(usuarios, sesion).apply {
            onUsuarioChange(usuario)
            onContrasenaChange(contrasena)
            iniciarSesion()
        }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun registroCon(nombre: String, usuario: String, contrasena: String): RegistroViewModel =
        RegistroViewModel(usuarios).apply {
            onNombreChange(nombre)
            onUsuarioChange(usuario)
            onContrasenaChange(contrasena)
            onConfirmarChange(contrasena)
            registrar()
        }

    // ---------------------------------------------------------------- iniciar sesión

    @Test
    fun iniciarSesion_conCredencialesCorrectas_guardaLaSesion() {
        val viewModel = loginCon("camila", "Segura#2026")

        assertTrue(viewModel.uiState.value.ingresoExitoso)
        assertEquals("camila", sesion.usuarioActual())
    }

    @Test
    fun iniciarSesion_conContrasenaIncorrecta_muestraErrorYNoAbreSesion() {
        val viewModel = loginCon("camila", "Otra#2026")

        assertTrue(viewModel.uiState.value.credencialesInvalidas)
        assertEquals(1, viewModel.uiState.value.intentosFallidos)
        assertNull(sesion.usuarioActual())
    }

    @Test
    fun iniciarSesion_conCamposVacios_muestraErroresSinConsultarAlRepositorio() {
        val viewModel = loginCon("", "")

        assertEquals("Ingresa tu usuario.", viewModel.uiState.value.errorUsuario)
        assertEquals("Ingresa tu contraseña.", viewModel.uiState.value.errorContrasena)
        assertFalse(viewModel.uiState.value.credencialesInvalidas)
    }

    @Test
    fun iniciarSesion_contraElServidor_abreSesionConLaCuentaDeUsuariosService() {
        val viewModel = LoginViewModel(remoto, sesion).apply {
            onUsuarioChange(" camila ")
            onContrasenaChange("Segura#2026")
            iniciarSesion()
        }

        assertTrue(viewModel.uiState.value.ingresoExitoso)
        assertEquals("camila", sesion.usuarioActual())
    }

    // ---------------------------------------------------------------- crear cuenta

    @Test
    fun registrar_conDatosValidos_creaElUsuario() = runBlocking {
        val viewModel = registroCon("Ana Torres", "atorres", "Clave#2026x")

        assertTrue(viewModel.uiState.value.registroExitoso)
        assertEquals("Ana Torres", usuarios.nombre("atorres"))
    }

    @Test
    fun registrar_conUsuarioQueYaExiste_avisaQueEstaOcupado() {
        val viewModel = registroCon("Camila Rojas", "camila", "Clave#2026x")

        assertFalse(viewModel.uiState.value.registroExitoso)
        assertEquals("Ese usuario ya existe.", viewModel.uiState.value.errorUsuario)
    }

    @Test
    fun registrar_conContrasenaDebil_noCreaLaCuenta() = runBlocking {
        val viewModel = registroCon("Ana Torres", "atorres", "clave123")

        assertEquals("La clave debe incluir al menos una letra mayúscula.", viewModel.uiState.value.errorContrasena)
        assertNull(usuarios.nombre("atorres"))
    }

    @Test
    fun contrasenaRegistro_conDatosPersonalesOElNombreDeLaApp_esRechazada() {
        assertEquals("No incluyas tu nombre ni tu usuario.", Validaciones.contrasenaRegistro("Ana Torres", "atorres", "Torres#2026"))
        assertEquals("No incluyas el nombre de la aplicación.", Validaciones.contrasenaRegistro("Ana", "atorres", "Electra#2026"))
        assertEquals("La clave debe incluir un carácter especial: ! @ # $ % & * ?", Validaciones.contrasenaRegistro("Ana", "ana1", "Clave2026x"))
        assertNull(Validaciones.contrasenaRegistro("Ana Torres", "atorres", "Clave#2026x"))
    }

    @Test
    fun usuarioRegistro_conEspaciosOCaracteresInvalidos_esRechazado() {
        assertEquals("El usuario no debe contener espacios.", Validaciones.usuarioRegistro("ana torres"))
        assertEquals("El usuario debe tener al menos 4 caracteres.", Validaciones.usuarioRegistro("ana"))
        assertEquals("Usa solo letras, números, punto, guion o guion bajo.", Validaciones.usuarioRegistro("ana#torres"))
        assertNull(Validaciones.usuarioRegistro("ana.torres"))
    }

    // ---------------------------------------------------------------- recuperar contraseña

    @Test
    fun restablecer_conDatosCorrectos_cambiaLaContrasena() = runBlocking {
        val viewModel = RecuperarContrasenaViewModel(usuarios).apply {
            onUsuarioChange("camila")
            onNombreChange("Camila Rojas")
            onContrasenaChange("Nueva#2027x")
            onConfirmarChange("Nueva#2027x")
            restablecer()
        }

        assertTrue(viewModel.uiState.value.restablecida)
        assertTrue(usuarios.validar("camila", "Nueva#2027x"))
    }

    @Test
    fun restablecer_conNombreQueNoCoincide_noCambiaLaContrasena() = runBlocking {
        val viewModel = RecuperarContrasenaViewModel(usuarios).apply {
            onUsuarioChange("camila")
            onNombreChange("Otra Persona")
            onContrasenaChange("Nueva#2027x")
            onConfirmarChange("Nueva#2027x")
            restablecer()
        }

        assertTrue(viewModel.uiState.value.datosNoCoinciden)
        assertTrue(usuarios.validar("camila", "Segura#2026"))
    }

    // ---------------------------------------------------------------- usuarios-service (servidor falso)

    @Test
    fun remoto_registrar_devuelveVerdaderoCon201YFalsoSiElUsuarioYaExiste() = runBlocking {
        assertTrue(remoto.registrar("Ana Torres", "atorres", "Clave#2026x", Rol.TECNICO))
        assertFalse(remoto.registrar("Camila Rojas", "camila", "Clave#2026x", Rol.TECNICO)) // 409
        assertEquals("tecnico", api.cuentas.getValue("atorres").rol)
    }

    @Test
    fun remoto_validar_respetaLaRespuestaDelServidor() = runBlocking {
        assertTrue(remoto.validar("camila", "Segura#2026"))
        assertFalse(remoto.validar("camila", "mala")) // 401
    }

    @Test
    fun remoto_rolNombreYCreacion_salenDeUnaSolaConsultaAlServidor() = runBlocking {
        assertEquals(Rol.SUPERVISOR, remoto.rol("camila"))
        assertEquals("Camila Rojas", remoto.nombre("camila"))
        assertEquals(777L, remoto.creadaEn("camila"))
        assertEquals(1, api.llamadasObtener) // los otros dos datos se tomaron de la caché
    }

    @Test
    fun remoto_usuarioInexistente_tomaElRolConMenosPermisos() = runBlocking {
        assertEquals(Rol.TECNICO, remoto.rol("nadie"))
        assertNull(remoto.nombre("nadie"))
        assertNull(remoto.nombre(" ")) // sin usuario no se consulta
    }

    @Test
    fun remoto_restablecerYCambiarContrasena_usanLosCodigos204Y401() = runBlocking {
        assertTrue(remoto.restablecer("camila", "camila rojas", "Nueva#2027x"))
        assertFalse(remoto.restablecer("nadie", "X", "Nueva#2027x"))
        assertFalse(remoto.cambiarContrasena("camila", "Segura#2026", "Otra#2028x")) // la actual ya cambió
        assertTrue(remoto.cambiarContrasena("camila", "Nueva#2027x", "Otra#2028x"))
    }

    @Test
    fun remoto_sinRed_devuelveFalsoYActivaElAvisoSinConexion() = runBlocking {
        api.caido = true

        assertFalse(remoto.validar("camila", "Segura#2026"))
        assertTrue(conexion.sinConexion.value)

        api.caido = false
        assertTrue(remoto.validar("camila", "Segura#2026"))
        assertFalse(conexion.sinConexion.value) // el aviso se apaga solo cuando el servidor vuelve
    }
}
