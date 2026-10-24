package com.example.electra.data

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface UsuariosRepository {
    /** Devuelve false si el usuario está vacío o ya existe. */
    suspend fun registrar(nombre: String, usuario: String, contrasena: String): Boolean
    suspend fun validar(usuario: String, contrasena: String): Boolean
}

class LocalUsuariosRepository(
    private val context: Context,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : UsuariosRepository {

    override suspend fun registrar(nombre: String, usuario: String, contrasena: String): Boolean =
        withContext(io) {
            UsuariosLocal.registrar(context, nombre, usuario, contrasena)
        }

    override suspend fun validar(usuario: String, contrasena: String): Boolean = withContext(io) {
        UsuariosLocal.validar(context, usuario, contrasena)
    }
}
