package com.example.electra.data

import android.content.Context
import androidx.core.content.edit
import com.example.electra.model.Avatar
import com.example.electra.model.Rol
import com.example.electra.model.UsuarioResumen
import java.security.MessageDigest

object UsuariosLocal {

    private const val PREFS = "electra_usuarios"

    fun registrar(
        context: Context,
        nombre: String,
        usuario: String,
        contrasena: String,
        rol: Rol = Rol.POR_DEFECTO
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
            .putString("rol_$clave", rol.clave)
            .commit()
    }

    fun rol(context: Context, usuario: String): Rol =
        Rol.desdeClave(
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("rol_${usuario.trim()}", null)
        )

    fun actualizarRol(context: Context, usuario: String, rol: Rol): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val clave = usuario.trim()
        if (!prefs.contains("hash_$clave")) return false

        return prefs.edit()
            .putString("rol_$clave", rol.clave)
            .commit()
    }

    /** Todas las cuentas registradas en el dispositivo, ordenadas por nombre. */
    fun listar(context: Context): List<UsuarioResumen> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.all.keys
            .filter { it.startsWith("hash_") }
            .map { it.removePrefix("hash_") }
            .map { usuario ->
                UsuarioResumen(
                    usuario = usuario,
                    nombre = prefs.getString("nombre_$usuario", null) ?: usuario,
                    rol = rol(context, usuario),
                    avatar = avatar(context, usuario)
                )
            }
            .sortedBy { it.nombre.lowercase() }
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

    fun restablecer(
        context: Context,
        usuario: String,
        nombre: String,
        nueva: String
    ): Boolean {
        val prefs = context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )

        val clave = usuario.trim()
        if (!prefs.contains("hash_$clave")) return false

        val registrado = prefs.getString("nombre_$clave", null) ?: return false
        if (!registrado.equals(nombre.trim(), ignoreCase = true)) return false

        return prefs.edit()
            .putString("hash_$clave", hash(nueva))
            .commit()
    }

    fun cambiarContrasena(
        context: Context,
        usuario: String,
        actual: String,
        nueva: String
    ): Boolean {
        if (!validar(context, usuario, actual)) return false

        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("hash_${usuario.trim()}", hash(nueva))
            .commit()
    }

    fun actualizarNombre(context: Context, usuario: String, nombre: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val clave = usuario.trim()
        if (!prefs.contains("hash_$clave")) return false

        return prefs.edit()
            .putString("nombre_$clave", nombre.trim())
            .commit()
    }

    fun avatar(context: Context, usuario: String): Avatar {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val clave = usuario.trim()
        return Avatar(
            color = prefs.getString("avatar_color_$clave", null) ?: Avatar.COLOR_POR_DEFECTO,
            fondo = prefs.getString("avatar_fondo_$clave", null) ?: Avatar.FONDO_POR_DEFECTO,
            foto = prefs.getString("avatar_foto_$clave", null)
        )
    }

    fun actualizarAvatar(context: Context, usuario: String, avatar: Avatar): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val clave = usuario.trim()
        if (!prefs.contains("hash_$clave")) return false

        return prefs.edit()
            .putString("avatar_color_$clave", avatar.color)
            .putString("avatar_fondo_$clave", avatar.fondo)
            .putString("avatar_foto_$clave", avatar.foto)
            .remove("avatar_icono_$clave")
            .commit()
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