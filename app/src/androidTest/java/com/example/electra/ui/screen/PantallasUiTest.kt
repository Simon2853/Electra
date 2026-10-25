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
import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.device.ResultadoUbicacion
import com.example.electra.model.Protocolo
import com.example.electra.model.Prueba
import com.example.electra.navigation.Routes
import com.example.electra.ui.viewmodel.DetalleProtocoloViewModel
import com.example.electra.ui.viewmodel.HistorialEquipoViewModel
import com.example.electra.ui.viewmodel.HomeViewModel
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

    @Test
    fun login_credencialesInvalidasMuestraError() {
        var ingreso = false
        val vm = LoginViewModel(usuarios, SesionEnMemoria())
        compose.setContent { Login(viewModel = vm, onLoginClick = { ingreso = true }) }

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
        val vm = LoginViewModel(usuarios, SesionEnMemoria())
        compose.setContent { Login(viewModel = vm, onLoginClick = { ingreso = true }) }

        compose.onNodeWithText("Usuario").performTextInput("anatest")
        compose.onNodeWithText("Contraseña").performTextInput("Segura#2026")
        compose.onNode(botonIniciarSesion).performClick()

        compose.waitUntil(5_000) { ingreso }
    }

    private fun llenarRegistro(nombre: String, usuario: String, clave: String, confirmar: String) {
        compose.onNodeWithText("Nombre completo").performTextInput(nombre)
        compose.onNodeWithText("Nombre de usuario").performTextInput(usuario)
        compose.onNodeWithText("Contraseña").performTextInput(clave)
        compose.onNodeWithText("Confirmar contraseña").performScrollTo().performTextInput(confirmar)
        compose.onNode(hasText("Crear cuenta") and hasClickAction()).performScrollTo().performClick()
    }

    private fun mostrarRegistro(onExito: () -> Unit = {}) {
        val vm = RegistroViewModel(usuarios)
        compose.setContent { Registro(viewModel = vm, onRegistroExitoso = onExito) }
    }

    @Test
    fun registro_camposVaciosMuestraError() {
        mostrarRegistro()
        compose.onNode(hasText("Crear cuenta") and hasClickAction()).performScrollTo().performClick()
        compose.esperarTexto("Ingresa tu nombre completo.")
        compose.esperarTexto("Ingresa un nombre de usuario.")
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

    private fun homeVm() = HomeViewModel(
        PruebasRepositoryEnMemoria(), ProtocolosRepositoryEnMemoria(), usuarios, SesionEnMemoria()
    )

    @Test
    fun home_nuevaPruebaNavegaARegistrarPrueba() {
        var ruta = ""
        compose.setContent { Home(viewModel = homeVm(), onNavigate = { ruta = it }) }

        compose.onNodeWithText("Registrar nueva prueba").performClick()
        assertEquals("registrar_prueba/TR-001", ruta)
    }

    @Test
    fun home_equiposNavegaALista() {
        var ruta = ""
        compose.setContent { Home(viewModel = homeVm(), onNavigate = { ruta = it }) }

        compose.onAllNodesWithText("Equipos")[0].performClick()
        assertEquals(Routes.EQUIPOS, ruta)
    }

    @Test
    fun equipos_muestraLosCincoEquipos() {
        compose.setContent { Equipos() }
        compose.onNodeWithText("5 equipos encontrados").assertExists()
    }

    @Test
    fun equipos_buscarPorCodigoFiltra() {
        compose.setContent { Equipos() }
        compose.onNodeWithText("Buscar equipo").performTextInput("TR-")
        compose.onNodeWithText("2 equipos encontrados").assertExists()
    }

    @Test
    fun equipos_buscarPorClienteFiltra() {
        compose.setContent { Equipos() }
        compose.onNodeWithText("Buscar equipo").performTextInput("Pacífico")
        compose.onNodeWithText("2 equipos encontrados").assertExists()
    }

    @Test
    fun equipos_busquedaSinResultadosMuestraMensaje() {
        compose.setContent { Equipos() }
        compose.onNodeWithText("Buscar equipo").performTextInput("XYZ")
        compose.onNodeWithText("No se encontraron equipos").assertExists()
    }

    @Test
    fun equipos_tocarEquipoDevuelveSuCodigo() {
        var codigo = ""
        compose.setContent { Equipos(onEquipoClick = { codigo = it }) }

        compose.onNodeWithText("Buscar equipo").performTextInput("RL")
        compose.onNodeWithText("RL-004").performClick()
        assertEquals("RL-004", codigo)
    }

    private fun registrarVm(repo: PruebasRepositoryEnMemoria) =
        RegistrarPruebaViewModel(repo, { ResultadoUbicacion.NoDisponible }, "RL-004")

    @Test
    fun registrarPrueba_medicionInvalidaMuestraErrorEnElCampo() {
        compose.setContent { RegistrarPrueba(viewModel = registrarVm(PruebasRepositoryEnMemoria())) }

        compose.onNodeWithText("Medición").performScrollTo().performTextInput("abc")
        compose.onNodeWithText("Guardar prueba").performScrollTo().performClick()

        compose.esperarTexto("La medición debe ser un número (ej: 125.5).")
    }

    @Test
    fun registrarPrueba_sinMedicionNoGuarda() {
        val repo = PruebasRepositoryEnMemoria()
        var volvio = false
        compose.setContent {
            RegistrarPrueba(viewModel = registrarVm(repo), onPruebaGuardada = { volvio = true })
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
            RegistrarPrueba(viewModel = registrarVm(repo), onPruebaGuardada = { volvio = true })
        }

        compose.onNodeWithText("Medición").performScrollTo().performTextInput("850")
        compose.onNodeWithText("Guardar prueba").performScrollTo().performClick()

        compose.waitUntil(5_000) { volvio }
        val guardada = repo.pruebas.single()
        assertEquals("RL-004", guardada.equipo)
        assertEquals("850", guardada.medicion)
        assertEquals("Aprobado", guardada.resultado)
    }

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
        val vm = HistorialEquipoViewModel(repo, "TR-002")
        compose.setContent {
            HistorialEquipo(viewModel = vm, onPruebaClick = { idTocado = it })
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
        val vm = HistorialEquipoViewModel(repo, "TR-001")
        compose.setContent { HistorialEquipo(viewModel = vm) }

        compose.esperarTexto("Cantidad de pruebas: 7")
        compose.onNodeWithText("Mostrando las últimas 5 de 7").assertExists()
        compose.onNodeWithText("TR-001 - Prueba 7").assertExists()
        compose.onNodeWithText("TR-001 - Prueba 2").assertDoesNotExist()
    }

    private fun repoProtocolos() = ProtocolosRepositoryEnMemoria(
        listOf(
            Protocolo(id = 1, numero = "#000124", equipo = "RL-004", fecha = "01/10/2026", estado = "Registrado en terreno"),
            Protocolo(id = 2, numero = "#000125", equipo = "TR-001", fecha = "02/10/2026", estado = "Formalizado")
        )
    )

    @Test
    fun protocolos_listaYAbreDetalleSinNumeral() {
        var numero = ""
        val vm = ProtocolosViewModel(repoProtocolos())
        compose.setContent {
            Protocolos(viewModel = vm, onProtocolClick = { numero = it })
        }

        compose.esperarTexto("2 protocolo(s) encontrado(s)")
        compose.onNodeWithText("#000124").performClick()
        assertEquals("000124", numero)
    }

    @Test
    fun detalleProtocolo_formalizar() {
        val repo = repoProtocolos()
        val vm = DetalleProtocoloViewModel(repo, PruebasRepositoryEnMemoria(), "000124")
        compose.setContent { DetalleProtocolo(viewModel = vm) }

        compose.esperarTexto("Registrado en terreno")
        compose.onNodeWithText("Formalizar protocolo").performScrollTo().performClick()

        compose.esperarTexto("Protocolo formalizado. Puedes compartirlo con el cliente desde el ícono superior.")
        compose.onNodeWithText("Formalizar protocolo").assertDoesNotExist()
        assertEquals("Formalizado", repo.protocolos.first { it.numero == "#000124" }.estado)
    }

    @Test
    fun detalleProtocolo_inexistenteMuestraMensaje() {
        val vm = DetalleProtocoloViewModel(repoProtocolos(), PruebasRepositoryEnMemoria(), "999999")
        compose.setContent { DetalleProtocolo(viewModel = vm) }
        compose.esperarTexto("No se encontró el protocolo.")
    }
}
