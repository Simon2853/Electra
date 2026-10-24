package com.example.electra.data

import android.content.Context
import androidx.core.content.edit

class SesionPreferencias(context: Context) {

    private val prefs = context.getSharedPreferences(
        "electra_sesion",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val CLAVE_ID_TECNICO = "id_tecnico"
        private const val SIN_SESION = -1L
    }

    fun guardar(idTecnico: Long) {
        prefs.edit {
            putLong(CLAVE_ID_TECNICO, idTecnico)
        }
    }

    fun leer(): Long {
        return prefs.getLong(
            CLAVE_ID_TECNICO,
            SIN_SESION
        )
    }

    fun haySesion(): Boolean {
        return leer() != SIN_SESION
    }

    fun borrar() {
        prefs.edit {
            remove(CLAVE_ID_TECNICO)
        }
    }
}
