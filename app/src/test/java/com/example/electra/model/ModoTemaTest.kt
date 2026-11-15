package com.example.electra.model

import com.example.electra.data.AjustesEnMemoria
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModoTemaTest {

    @Test
    fun sistema_sigueAlTelefono() {
        assertTrue(ModoTema.SISTEMA.esOscuro(sistemaOscuro = true))
        assertFalse(ModoTema.SISTEMA.esOscuro(sistemaOscuro = false))
    }

    @Test
    fun claroYOscuro_ignoranAlSistema() {
        assertFalse(ModoTema.CLARO.esOscuro(sistemaOscuro = true))
        assertTrue(ModoTema.OSCURO.esOscuro(sistemaOscuro = false))
    }

    @Test
    fun desdeClave_recuperaElGuardadoYUsaSistemaPorDefecto() {
        ModoTema.entries.forEach { assertEquals(it, ModoTema.desdeClave(it.clave)) }
        assertEquals(ModoTema.SISTEMA, ModoTema.desdeClave(null))
        assertEquals(ModoTema.SISTEMA, ModoTema.desdeClave("desconocido"))
    }

    @Test
    fun ajustes_cambiarTemaSePublicaAlInstante() {
        val ajustes = AjustesEnMemoria()
        assertEquals(ModoTema.SISTEMA, ajustes.tema.value)

        ajustes.cambiarTema(ModoTema.OSCURO)

        assertEquals(ModoTema.OSCURO, ajustes.tema.value)
    }
}
