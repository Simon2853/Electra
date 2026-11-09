package com.example.electra.data

import android.content.Context
import com.example.electra.model.Avatar
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface UsuariosRepository {

    suspend fun registrar(nombre: String, usuario: String, contrasena: String): Boolean
    suspend fun validar(usuario: String, contrasena: String): Boolean

    suspend fun restablecer(usuario: String, nombre: String, nueva: String): Boolean

    suspend fun cambiarContrasena(usuario: String, actual: String, nueva: String): Boolean

    suspend fun actualizarNombre(usuario: String, nombre: String): Boolean

    suspend fun avatar(usuario: String): Avatar

    suspend fun actualizarAvatar(usuario: String, avatar: Avatar): Boolean

    suspend fun nombre(usuario: String): String?
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

    override suspend fun restablecer(usuario: String, nombre: String, nueva: String): Boolean =
        withContext(io) {
            UsuariosLocal.restablecer(context, usuario, nombre, nueva)
        }

    override suspend fun cambiarContrasena(usuario: String, actual: String, nueva: String): Boolean =
        withContext(io) {
            UsuariosLocal.cambiarContrasena(context, usuario, actual, nueva)
        }

    override suspend fun actualizarNombre(usuario: String, nombre: String): Boolean = withContext(io) {
        UsuariosLocal.actualizarNombre(context, usuario, nombre)
    }

    override suspend fun avatar(usuario: String): Avatar = withContext(io) {
        UsuariosLocal.avatar(context, usuario)
    }

    override suspend fun actualizarAvatar(usuario: String, avatar: Avatar): Boolean = withContext(io) {
        UsuariosLocal.actualizarAvatar(context, usuario, avatar)
    }

    override suspend fun nombre(usuario: String): String? = withContext(io) {
        UsuariosLocal.nombre(context, usuario)
    }
}
