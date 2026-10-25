package com.example.electra.data

import android.content.Context
import androidx.core.content.edit
import java.security.MessageDigest

object UsuariosLocal {

    private const val PREFS = "electra_usuarios"

    fun registrar(
        context: Context,
        nombre: String,
        usuario: String,
        contrasena: String
    ): Boolean {
        val prefs = context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )

        val clave = usuario.trim()

        if (clave.isBlank()) return false
        if (prefs.contains("hash_$clave")) return false

        return prefs.edit()
            .putString("nombre_$clave", nombre.trim())
            .putString("hash_$clave", hash(contrasena))
            .commit()
    }

    fun validar(
        context: Context,
        usuario: String,
        contrasena: String
    ): Boolean {
        val prefs = context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )

        val clave = usuario.trim()
        val guardado = prefs.getString("hash_$clave", null)
            ?: return false

        return guardado == hash(contrasena)
    }

    fun nombre(context: Context, usuario: String): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("nombre_${usuario.trim()}", null)

    private fun hash(texto: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray(Charsets.UTF_8))

        return bytes.joinToString("") {
            "%02x".format(it.toInt() and 0xff)
        }
    }
}