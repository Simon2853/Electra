package com.example.electra.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RoutesTest {

    @Test
    fun detallePrueba_construyeRutaConId() {
        assertEquals("detalle_prueba/42", Routes.detallePrueba(42L))
    }

    @Test
    fun protocolo_construyeRutaConNumero() {
        assertEquals("protocolo/000125", Routes.protocolo("000125"))
    }

    @Test
    fun rutasConParametro_coincidenConSuRutaBase() {
        assertEquals("${Routes.DETALLE_EQUIPO}/{codigo}", Routes.DETALLE_EQUIPO_PARAM)
        assertEquals("${Routes.REGISTRAR_PRUEBA}/{codigo}", Routes.REGISTRAR_PRUEBA_PARAM)
        assertEquals("${Routes.HISTORIAL_EQUIPO}/{codigo}", Routes.HISTORIAL_EQUIPO_PARAM)
        assertEquals("${Routes.DETALLE_PRUEBA}/{idPrueba}", Routes.DETALLE_PRUEBA_PARAM)
        assertEquals("${Routes.PROTOCOLO}/{numero}", Routes.PROTOCOLO_PARAM)
        assertEquals("${Routes.CONFIRMACION_PRUEBA}/{idPrueba}", Routes.CONFIRMACION_PRUEBA_PARAM)
    }

    @Test
    fun protocolo_quitaElNumeral() {
        assertEquals("protocolo/000125", Routes.protocolo("#000125"))
    }

    @Test
    fun helpers_generanRutasConArgumento() {
        assertEquals("registrar_prueba/TR-001", Routes.registrarPrueba("TR-001"))
        assertEquals("historial_equipo/RL-004", Routes.historialEquipo("RL-004"))
        assertEquals("detalle_equipo/CE-008", Routes.detalleEquipo("CE-008"))
        assertEquals("confirmacion_prueba/7", Routes.confirmacionPrueba(7L))
    }

    @Test
    fun reemplazoDeCodigo_generaRutaNavegable() {
        val ruta = Routes.REGISTRAR_PRUEBA_PARAM.replace("{codigo}", "TR-001")
        assertEquals("registrar_prueba/TR-001", ruta)
        assertFalse(ruta.contains("{"))
    }

    @Test
    fun todasLasRutasSonUnicas() {
        val rutas = listOf(
            Routes.LOGIN, Routes.REGISTRO, Routes.HOME, Routes.EQUIPOS,
            Routes.DETALLE_EQUIPO_PARAM, Routes.REGISTRAR_PRUEBA_PARAM,
            Routes.CONFIRMACION_PRUEBA_PARAM, Routes.PROTOCOLOS, Routes.PROTOCOLO_PARAM,
            Routes.CREAR_PROTOCOLO, Routes.HISTORIAL_EQUIPO_PARAM,
            Routes.DETALLE_PRUEBA_PARAM, Routes.PERFIL
        )
        assertEquals(rutas.size, rutas.toSet().size)
    }
}
