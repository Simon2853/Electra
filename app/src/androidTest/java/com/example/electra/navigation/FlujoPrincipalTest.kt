package com.example.electra.navigation

import android.content.Context
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.electra.contextoDePrueba
import com.example.electra.data.AppContainer
import com.example.electra.limpiarAlmacenamiento
import com.example.electra.ui.screen.esperarTexto
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de punta a punta con el contenedor real (SQLite + SharedPreferences),
 * aislado con prefijo "test_" para no tocar los datos del dispositivo.
 */
@RunWith(AndroidJUnit4::class)
class FlujoPrincipalTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var contexto: Context

    @Before
    fun setUp() {
        contexto = contextoDePrueba()
        limpiarAlmacenamiento(contexto)
        val contenedor = AppContainer(contexto)
        compose.setContent { RootNavigation(contenedor) }
    }

    @After
    fun tearDown() {
        limpiarAlmacenamiento(contexto)
    }

    private fun registrarseEIngresar() {
        compose.onNodeWithText("¿No tienes cuenta? Regístrate").performClick()
        compose.onNodeWithText("Nombre completo").performTextInput("Ana Test")
        compose.onNodeWithText("Nombre de usuario").performTextInput("anatest")
        compose.onNodeWithText("Contraseña").performTextInput("Segura#2026")
        compose.onNodeWithText("Confirmar contraseña").performScrollTo().performTextInput("Segura#2026")
        compose.onNode(hasText("Crear cuenta") and hasClickAction()).performScrollTo().performClick()

        // Vuelve al login e inicia sesión
        compose.esperarTexto("¿No tienes cuenta? Regístrate")
        compose.onNodeWithText("Usuario").performTextInput("anatest")
        compose.onNodeWithText("Contraseña").performTextInput("Segura#2026")
        compose.onNode(hasText("Iniciar sesión") and hasClickAction()).performClick()

        compose.esperarTexto("Hola, Técnico")
    }

    @Test
    fun registroLoginYConsultaDeEquipo() {
        registrarseEIngresar()

        compose.onAllNodesWithText("Equipos")[0].performClick()
        compose.onNodeWithText("Selecciona el equipo a revisar").assertExists()
        compose.onNodeWithText("TR-002").performClick()
        compose.onNodeWithText("Detalle del equipo").assertExists()
    }

    @Test
    fun registrarPruebaYVerlaEnElHistorial() {
        registrarseEIngresar()

        compose.onNodeWithText("Registrar nueva prueba").performClick()
        compose.onNodeWithText("Ej: 125.5").performScrollTo().performTextInput("1500")
        compose.onNodeWithText("Guardar prueba").performScrollTo().performClick()

        // Al guardar vuelve al Home; la tarjeta "Pruebas" abre el historial de TR-001
        compose.esperarTexto("Hola, Técnico")
        compose.onAllNodesWithText("Pruebas")[0].performClick()

        compose.esperarTexto("Cantidad de pruebas: 1")
        compose.onNodeWithText("Última medición: 1500 MΩ").assertExists()
    }

    @Test
    fun crearProtocoloYVerloEnLaLista() {
        registrarseEIngresar()

        compose.onAllNodesWithText("Protocolos")[0].performClick()
        compose.esperarTexto("4 protocolo(s) encontrado(s)")

        compose.onNodeWithContentDescription("Nuevo Protocolo").performClick()
        // Espera a que el ViewModel sugiera el número: 4 protocolos semilla + 126
        compose.esperarTexto("#000130")
        compose.onNodeWithText("Responsable / Inspector").performTextInput("Ana Test")
        compose.onNodeWithText("Guardar Protocolo").performScrollTo().performClick()

        compose.esperarTexto("5 protocolo(s) encontrado(s)")
        compose.onNodeWithText("#000130").assertExists()
    }
}
