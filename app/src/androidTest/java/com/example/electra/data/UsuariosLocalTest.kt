package com.example.electra.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.electra.contextoDePrueba
import com.example.electra.limpiarAlmacenamiento
import com.example.electra.model.Avatar
import org.junit.After
import org.junit.Assert.assertEquals
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
    fun restablecer_conNombreCorrectoCambiaLaContrasena() {
        UsuariosLocal.registrar(contexto, "Ana Test", "anatest", "Segura#2026")

        assertTrue(UsuariosLocal.restablecer(contexto, "anatest", "ana test", "Nueva#2027x"))
        assertTrue(UsuariosLocal.validar(contexto, "anatest", "Nueva#2027x"))
        assertFalse(UsuariosLocal.validar(contexto, "anatest", "Segura#2026"))
    }

    @Test
    fun restablecer_conNombreIncorrectoOUsuarioInexistenteFalla() {
        UsuariosLocal.registrar(contexto, "Ana Test", "anatest", "Segura#2026")

        assertFalse(UsuariosLocal.restablecer(contexto, "anatest", "Otra", "Nueva#2027x"))
        assertFalse(UsuariosLocal.restablecer(contexto, "nadie", "Ana Test", "Nueva#2027x"))
        assertTrue(UsuariosLocal.validar(contexto, "anatest", "Segura#2026"))
    }

    @Test
    fun cambiarContrasena_exigeLaContrasenaActual() {
        UsuariosLocal.registrar(contexto, "Ana Test", "anatest", "Segura#2026")

        assertFalse(UsuariosLocal.cambiarContrasena(contexto, "anatest", "Equivocada#1", "Nueva#2027x"))
        assertTrue(UsuariosLocal.validar(contexto, "anatest", "Segura#2026"))

        assertTrue(UsuariosLocal.cambiarContrasena(contexto, "anatest", "Segura#2026", "Nueva#2027x"))
        assertTrue(UsuariosLocal.validar(contexto, "anatest", "Nueva#2027x"))
    }

    @Test
    fun actualizarNombre_cambiaSoloElNombre() {
        UsuariosLocal.registrar(contexto, "Ana Test", "anatest", "Segura#2026")

        assertTrue(UsuariosLocal.actualizarNombre(contexto, "anatest", " Ana María "))
        assertEquals("Ana María", UsuariosLocal.nombre(contexto, "anatest"))
        assertTrue(UsuariosLocal.validar(contexto, "anatest", "Segura#2026"))
        assertFalse(UsuariosLocal.actualizarNombre(contexto, "nadie", "Otra"))
    }

    @Test
    fun avatar_usaElPorDefectoYGuardaElElegido() {
        UsuariosLocal.registrar(contexto, "Ana Test", "anatest", "Segura#2026")
        assertEquals(Avatar(), UsuariosLocal.avatar(contexto, "anatest"))

        val elegido = Avatar(color = "rojo", fondo = "crema", foto = "content://foto/perfil.jpg")
        assertTrue(UsuariosLocal.actualizarAvatar(contexto, "anatest", elegido))
        assertEquals(elegido, UsuariosLocal.avatar(contexto, "anatest"))

        assertTrue(UsuariosLocal.actualizarAvatar(contexto, "anatest", elegido.copy(foto = null)))
        assertEquals(null, UsuariosLocal.avatar(contexto, "anatest").foto)
        assertFalse(UsuariosLocal.actualizarAvatar(contexto, "nadie", Avatar()))
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
