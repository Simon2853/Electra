package com.example.electra.data

import android.content.Context
import androidx.core.content.edit

interface SesionRepository {

    fun usuarioActual(): String?
    fun iniciar(usuario: String)
    fun cerrar()
}

class SesionPreferencias(context: Context) : SesionRepository {

    private val prefs = context.getSharedPreferences(
        "electra_sesion",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val CLAVE_USUARIO = "usuario"
    }

    override fun usuarioActual(): String? = prefs.getString(CLAVE_USUARIO, null)

    override fun iniciar(usuario: String) {
        prefs.edit { putString(CLAVE_USUARIO, usuario) }
    }

    override fun cerrar() {
        prefs.edit { remove(CLAVE_USUARIO) }
    }
}
