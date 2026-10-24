package com.example.electra.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.electra.contextoDePrueba
import com.example.electra.limpiarAlmacenamiento
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsuariosLocalTest {

    private lateinit var contexto: Context

    @Before
    fun setUp() {
        contexto = contextoDePrueba()
        limpiarAlmacenamiento(contexto)
    }

    @After
    fun tearDown() {
        limpiarAlmacenamiento(contexto)
    }

    @Test
    fun registrar_yValidarConCredencialesCorrectas() {
        assertTrue(UsuariosLocal.registrar(contexto, "Ana Test", "anatest", "Segura#2026"))
        assertTrue(UsuariosLocal.validar(contexto, "anatest", "Segura#2026"))
    }

    @Test
    fun validar_conContrasenaIncorrectaFalla() {
        UsuariosLocal.registrar(contexto, "Ana Test", "anatest", "Segura#2026")
        assertFalse(UsuariosLocal.validar(contexto, "anatest", "segura#2026"))
    }

    @Test
    fun validar_usuarioInexistenteFalla() {
        assertFalse(UsuariosLocal.validar(contexto, "nadie", "Segura#2026"))
    }

    @Test
    fun registrar_usuarioDuplicadoFalla() {
        assertTrue(UsuariosLocal.registrar(contexto, "Ana", "anatest", "Segura#2026"))
        assertFalse(UsuariosLocal.registrar(contexto, "Otra", "anatest", "Otra#2026x"))
    }

    @Test
    fun registrar_usuarioVacioFalla() {
        assertFalse(UsuariosLocal.registrar(contexto, "Ana", "   ", "Segura#2026"))
    }

    @Test
    fun usuario_distingueMayusculasPeroIgnoraEspaciosExternos() {
        UsuariosLocal.registrar(contexto, "Ana", "AnaTest", "Segura#2026")

        assertTrue(UsuariosLocal.validar(contexto, "  AnaTest ", "Segura#2026"))
        assertFalse(UsuariosLocal.validar(contexto, "anatest", "Segura#2026"))
    }

    @Test
    fun contrasena_noSeGuardaEnTextoPlano() {
        UsuariosLocal.registrar(contexto, "Ana", "anatest", "Segura#2026")
        val guardado = contexto
            .getSharedPreferences("electra_usuarios", Context.MODE_PRIVATE)
            .getString("hash_anatest", null)

        assertNotEquals("Segura#2026", guardado)
        assertTrue(guardado!!.matches(Regex("[0-9a-f]{64}")))
    }
}
