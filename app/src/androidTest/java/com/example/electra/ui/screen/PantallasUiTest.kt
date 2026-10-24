package com.example.electra.ui.screen

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.electra.data.ProtocolosRepositoryEnMemoria
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.model.Protocolo
import com.example.electra.model.Prueba
import com.example.electra.navigation.Routes
import com.example.electra.ui.viewmodel.DetalleProtocoloViewModel
import com.example.electra.ui.viewmodel.HistorialEquipoViewModel
import com.example.electra.ui.viewmodel.LoginViewModel
import com.example.electra.ui.viewmodel.ProtocolosViewModel
import com.example.electra.ui.viewmodel.RegistrarPruebaViewModel
import com.example.electra.ui.viewmodel.RegistroViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Espera a que aparezca un nodo: las operaciones de los ViewModels son asíncronas. */
fun ComposeContentTestRule.esperarNodo(matcher: SemanticsMatcher, timeoutMs: Long = 5_000) {
    waitUntil(timeoutMs) { onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
}

fun ComposeContentTestRule.esperarTexto(texto: String) = esperarNodo(hasText(texto))

@RunWith(AndroidJUnit4::class)
class PantallasUiTest {

    @get:Rule
    val compose = createComposeRule()

    private val usuarios = UsuariosRepositoryEnMemoria()

    private val botonIniciarSesion = hasText("Iniciar sesión") and hasClickAction()

    // --- LOGIN ---

    @Test
    fun login_credencialesInvalidasMuestraError() {
        var ingreso = false
        compose.setContent { Login(viewModel = LoginViewModel(usuarios), onLoginClick = { ingreso = true }) }

        compose.onNodeWithText("Usuario").performTextInput("nadie")
        compose.onNodeWithText("Contraseña").performTextInput("Clave#1234")
        compose.onNode(botonIniciarSesion).performClick()

        compose.esperarTexto("Usuario o contraseña incorrectos.")
        assertFalse(ingreso)
    }

    @Test
    fun login_credencialesValidasIngresa() {
        runBlocking { usuarios.registrar("Ana Test", "anatest", "Segura#2026") }
        var ingreso = false
        compose.setContent { Login(viewModel = LoginViewModel(usuarios), onLoginClick = { ingreso = true }) }

        compose.onNodeWithText("Usuario").performTextInput("anatest")
        compose.onNodeWithText("Contraseña").performTextInput("Segura#2026")
        compose.onNode(botonIniciarSesion).performClick()

        compose.waitUntil(5_000) { ingreso }
    }

    // --- REGISTRO ---

    private fun llenarRegistro(nombre: String, usuario: String, clave: String, confirmar: String) {
        compose.onNodeWithText("Nombre completo").performTextInput(nombre)
        compose.onNodeWithText("Nombre de usuario").performTextInput(usuario)
        compose.onNodeWithText("Contraseña").performTextInput(clave)
        compose.onNodeWithText("Confirmar contraseña").performScrollTo().performTextInput(confirmar)
        compose.onNode(hasText("Crear cuenta") and hasClickAction()).performScrollTo().performClick()
    }

    private fun mostrarRegistro(onExito: () -> Unit = {}) {
        compose.setContent { Registro(viewModel = RegistroViewModel(usuarios), onRegistroExitoso = onExito) }
    }

    @Test
    fun registro_camposVaciosMuestraError() {
        mostrarRegistro()
        compose.onNode(hasText("Crear cuenta") and hasClickAction()).performScrollTo().performClick()
        compose.esperarTexto("Completa todos los campos.")
    }

    @Test
    fun registro_contrasenaDebilMuestraError() {
        mostrarRegistro()
        llenarRegistro("Ana Test", "anatest", "corta", "corta")
        compose.esperarTexto("La clave debe tener al menos 8 caracteres.")
    }

    @Test
    fun registro_contrasenaConNombreDeLaAppMuestraError() {
        mostrarRegistro()
        llenarRegistro("Ana Test", "anatest", "Electra#2026", "Electra#2026")
        compose.esperarTexto("No incluyas el nombre de la aplicación.")
    }

    @Test
    fun registro_contrasenasDistintasMuestraError() {
        mostrarRegistro()
        llenarRegistro("Ana Test", "anatest", "Segura#2026", "Segura#2027")
        compose.esperarTexto("Las contraseñas no coinciden.")
    }

    @Test
    fun registro_exitosoGuardaUsuario() {
        var exito = false
        mostrarRegistro { exito = true }
        llenarRegistro("Ana Test", "anatest", "Segura#2026", "Segura#2026")

        compose.waitUntil(5_000) { exito }
        assertTrue(runBlocking { usuarios.validar("anatest", "Segura#2026") })
    }

    @Test
    fun registro_usuarioExistenteMuestraError() {
        runBlocking { usuarios.registrar("Otro", "anatest", "Otra#2026x") }
        mostrarRegistro()
        llenarRegistro("Ana Test", "anatest", "Segura#2026", "Segura#2026")
        compose.esperarTexto("Ese usuario ya existe.")
    }

    // --- HOME ---

    @Test
    fun home_nuevaPruebaNavegaARegistrarPrueba() {
        var ruta = ""
        compose.setContent { Home(onNavigate = { ruta = it }) }

        compose.onNodeWithText("Registrar nueva prueba").performClick()
        assertEquals("registrar_prueba/TR-001", ruta)
    }

    @Test
    fun home_equiposNavegaALista() {
        var ruta = ""
        compose.setContent { Home(onNavigate = { ruta = it }) }

        compose.onAllNodesWithText("Equipos")[0].performClick()
        assertEquals(Routes.EQUIPOS, ruta)
    }

    // --- EQUIPOS ---

    @Test
    fun equipos_muestraLosCincoEquipos() {
        compose.setContent { Equipos() }
        compose.onNodeWithText("5 equipos encontrados").assertExists()
    }

    @Test
    fun equipos_buscarPorCodigoFiltra() {
        compose.setContent { Equipos() }
        compose.onNodeWithText("Buscar por código").performTextInput("tr")
        compose.onNodeWithText("2 equipos encontrados").assertExists()
    }

    @Test
    fun equipos_busquedaSinResultadosMuestraMensaje() {
        compose.setContent { Equipos() }
        compose.onNodeWithText("Buscar por código").performTextInput("XYZ")
        compose.onNodeWithText("No se encontraron equipos").assertExists()
    }

    @Test
    fun equipos_tocarEquipoDevuelveSuCodigo() {
        var codigo = ""
        compose.setContent { Equipos(onEquipoClick = { codigo = it }) }

        compose.onNodeWithText("Buscar por código").performTextInput("RL")
        compose.onNodeWithText("RL-004").performClick()
        assertEquals("RL-004", codigo)
    }

    // --- REGISTRAR PRUEBA ---

    @Test
    fun registrarPrueba_sinMedicionNoGuarda() {
        val repo = PruebasRepositoryEnMemoria()
        var volvio = false
        compose.setContent {
            RegistrarPrueba(viewModel = RegistrarPruebaViewModel(repo), codigoEquipo = "RL-004", onVolverClick = { volvio = true })
        }

        compose.onNodeWithText("Guardar prueba").performScrollTo().performClick()
        compose.waitForIdle()

        assertTrue(repo.pruebas.isEmpty())
        assertFalse(volvio)
    }

    @Test
    fun registrarPrueba_conMedicionGuardaYVuelve() {
        val repo = PruebasRepositoryEnMemoria()
        var volvio = false
        compose.setContent {
            RegistrarPrueba(viewModel = RegistrarPruebaViewModel(repo), codigoEquipo = "RL-004", onVolverClick = { volvio = true })
        }

        compose.onNodeWithText("Ej: 125.5").performScrollTo().performTextInput("850")
        compose.onNodeWithText("Guardar prueba").performScrollTo().performClick()

        compose.waitUntil(5_000) { volvio }
        val guardada = repo.pruebas.single()
        assertEquals("RL-004", guardada.equipo)
        assertEquals("850", guardada.medicion)
        assertEquals("Aprobado", guardada.resultado)
    }

    // --- HISTORIAL ---

    @Test
    fun historial_muestraPruebasDelEquipo() {
        val repo = PruebasRepositoryEnMemoria(
            listOf(
                Prueba(
                    id = 1, equipo = "TR-002", tipoPrueba = "Prueba de continuidad", fecha = "20/10/2026",
                    hora = "09:00", medicion = "0.5", unidad = "Ω", resultado = "Aprobado", observaciones = ""
                )
            )
        )
        var idTocado = -1L
        compose.setContent {
            HistorialEquipo(viewModel = HistorialEquipoViewModel(repo, "TR-002"), onPruebaClick = { idTocado = it })
        }

        compose.esperarTexto("Cantidad de pruebas: 1")
        compose.onNodeWithText("TR-002 - Prueba de continuidad").performClick()
        assertEquals(1L, idTocado)
    }

    @Test
    fun historial_conMasDeCincoPruebasMuestraSoloLasUltimas() {
        val repo = PruebasRepositoryEnMemoria(
            (1L..7L).map {
                Prueba(
                    id = it, equipo = "TR-001", tipoPrueba = "Prueba $it", fecha = "20/10/2026",
                    hora = "09:00", medicion = "$it", unidad = "Ω", resultado = "Aprobado", observaciones = ""
                )
            }
        )
        compose.setContent { HistorialEquipo(viewModel = HistorialEquipoViewModel(repo, "TR-001")) }

        compose.esperarTexto("Cantidad de pruebas: 7")
        compose.onNodeWithText("Mostrando las últimas 5 de 7").assertExists()
        compose.onNodeWithText("TR-001 - Prueba 7").assertExists()
        compose.onNodeWithText("TR-001 - Prueba 2").assertDoesNotExist()
    }

    // --- PROTOCOLOS ---

    private fun repoProtocolos() = ProtocolosRepositoryEnMemoria(
        listOf(
            Protocolo(id = 1, numero = "#000124", equipo = "RL-004", fecha = "01/10/2026", estado = "Registrado en terreno"),
            Protocolo(id = 2, numero = "#000125", equipo = "TR-001", fecha = "02/10/2026", estado = "Formalizado")
        )
    )

    @Test
    fun protocolos_listaYAbreDetalleSinNumeral() {
        var numero = ""
        compose.setContent {
            Protocolos(viewModel = ProtocolosViewModel(repoProtocolos()), onProtocolClick = { numero = it })
        }

        compose.esperarTexto("2 protocolo(s) encontrado(s)")
        compose.onNodeWithText("#000124").performClick()
        assertEquals("000124", numero)
    }

    @Test
    fun detalleProtocolo_formalizar() {
        val repo = repoProtocolos()
        compose.setContent { DetalleProtocolo(viewModel = DetalleProtocoloViewModel(repo, "000124")) }

        compose.esperarTexto("Estado actual: Registrado en terreno")
        compose.onNodeWithText("Formalizar Protocolo").performScrollTo().performClick()

        compose.esperarTexto("Estado actual: Formalizado")
        compose.onNodeWithText("Formalizar Protocolo").assertDoesNotExist()
        assertEquals("Formalizado", repo.protocolos.first { it.numero == "#000124" }.estado)
    }

    @Test
    fun detalleProtocolo_inexistenteMuestraMensaje() {
        compose.setContent { DetalleProtocolo(viewModel = DetalleProtocoloViewModel(repoProtocolos(), "999999")) }
        compose.esperarTexto("No se encontró el protocolo.")
    }
}
