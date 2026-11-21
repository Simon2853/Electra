package com.example.electra.data

import com.example.electra.model.Avatar
import com.example.electra.model.Rol
import com.example.electra.model.UsuarioResumen

interface UsuariosRepository {

    suspend fun registrar(
        nombre: String,
        usuario: String,
        contrasena: String,
        rol: Rol = Rol.POR_DEFECTO
    ): Boolean

    suspend fun validar(usuario: String, contrasena: String): Boolean

    suspend fun restablecer(usuario: String, nombre: String, nueva: String): Boolean

    suspend fun cambiarContrasena(usuario: String, actual: String, nueva: String): Boolean

    suspend fun actualizarNombre(usuario: String, nombre: String): Boolean

    suspend fun avatar(usuario: String): Avatar

    suspend fun actualizarAvatar(usuario: String, avatar: Avatar): Boolean

    suspend fun nombre(usuario: String): String?

    suspend fun rol(usuario: String): Rol

    suspend fun actualizarRol(usuario: String, rol: Rol): Boolean

    suspend fun listar(): List<UsuarioResumen>

    /** Momento en que se creó la cuenta (epoch ms); null en cuentas anteriores a guardar este dato. */
    suspend fun creadaEn(usuario: String): Long?
}

/** Rol de quien tiene la sesión abierta; sin sesión se trata como el rol con menos permisos. */
suspend fun UsuariosRepository.rolActual(sesion: SesionRepository): Rol =
    sesion.usuarioActual()?.let { rol(it) } ?: Rol.POR_DEFECTO
