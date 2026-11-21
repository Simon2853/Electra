package com.example.electra.data.remote

import com.example.electra.data.UsuariosRepository
import com.example.electra.model.Avatar
import com.example.electra.model.Rol
import com.example.electra.model.UsuarioResumen

/**
 * Cuentas guardadas en usuarios-service (antes estaban en SharedPreferences del teléfono).
 *
 * Varias pantallas preguntan por separado el nombre, el rol y el avatar de la misma cuenta; para no
 * hacer una llamada por cada dato, la respuesta se recuerda unos segundos ([vigenciaMs]) y se olvida
 * al modificar la cuenta.
 */
class RemotoUsuariosRepository(
    private val api: UsuariosApi,
    private val conexion: Conexion,
    private val archivos: ArchivosRemoto,
    private val eliminarLocal: (String) -> Unit = {},
    private val reloj: () -> Long = System::currentTimeMillis,
    private val vigenciaMs: Long = 30_000
) : UsuariosRepository {

    private val cache = mutableMapOf<String, Pair<UsuarioDto, Long>>()

    private fun recordar(dto: UsuarioDto?): UsuarioDto? = dto?.also { cache[it.usuario] = it to reloj() }

    private suspend fun cuenta(usuario: String): UsuarioDto? {
        val clave = usuario.trim()
        if (clave.isBlank()) return null
        cache[clave]?.let { (dto, cuando) -> if (reloj() - cuando < vigenciaMs) return dto }
        return recordar(conexion.llamar { api.obtener(clave) }.cuerpo())
    }

    override suspend fun registrar(nombre: String, usuario: String, contrasena: String, rol: Rol): Boolean {
        val respuesta = conexion.llamar { api.registrar(RegistroDto(nombre.trim(), usuario.trim(), contrasena, rol.clave)) }
        recordar(respuesta.cuerpo())
        return respuesta.respondio(201)
    }

    override suspend fun validar(usuario: String, contrasena: String): Boolean {
        val respuesta = conexion.llamar { api.login(LoginDto(usuario.trim(), contrasena)) }
        recordar(respuesta.cuerpo())
        return respuesta.respondio(200)
    }

    override suspend fun restablecer(usuario: String, nombre: String, nueva: String): Boolean =
        conexion.llamar { api.restablecer(RestablecerDto(usuario.trim(), nombre.trim(), nueva)) }.respondio(204)

    override suspend fun cambiarContrasena(usuario: String, actual: String, nueva: String): Boolean =
        conexion.llamar { api.cambiarContrasena(usuario.trim(), CambiarContrasenaDto(actual, nueva)) }.respondio(204)

    override suspend fun actualizarNombre(usuario: String, nombre: String): Boolean =
        recordar(conexion.llamar { api.actualizarNombre(usuario.trim(), NombreDto(nombre.trim())) }.cuerpo()) != null

    override suspend fun avatar(usuario: String): Avatar = cuenta(usuario)?.avatar.aAvatar()

    /** Si la foto es nueva (está en el teléfono), primero se sube a archivos-service y se guarda su ruta. */
    override suspend fun actualizarAvatar(usuario: String, avatar: Avatar): Boolean {
        val login = usuario.trim()
        val foto = when (val preparada = archivos.preparar(avatar.foto, "perfil", login)) {
            FotoPreparada.SinFoto -> null
            FotoPreparada.Fallo -> return false
            is FotoPreparada.Lista -> preparada
        }
        val actualizado = recordar(
            conexion.llamar { api.actualizarAvatar(login, AvatarDto(avatar.color, avatar.fondo, foto?.ruta)) }.cuerpo()
        )
        // Ya está en el servidor: la copia local sobra.
        if (actualizado != null) foto?.subidaDesde?.let(eliminarLocal)
        return actualizado != null
    }

    override suspend fun nombre(usuario: String): String? = cuenta(usuario)?.nombre

    override suspend fun rol(usuario: String): Rol = Rol.desdeClave(cuenta(usuario)?.rol)

    override suspend fun actualizarRol(usuario: String, rol: Rol): Boolean =
        recordar(conexion.llamar { api.actualizarRol(usuario.trim(), RolDto(rol.clave)) }.cuerpo()) != null

    override suspend fun listar(): List<UsuarioResumen> =
        conexion.llamar { api.listar() }.cuerpo().orEmpty().map { dto ->
            recordar(dto)
            UsuarioResumen(dto.usuario, dto.nombre, Rol.desdeClave(dto.rol), dto.avatar.aAvatar())
        }

    override suspend fun creadaEn(usuario: String): Long? = cuenta(usuario)?.creadaEn
}

/** La foto llega como ruta relativa; Coil necesita la URL completa. */
internal fun AvatarDto?.aAvatar(): Avatar = Avatar(
    color = this?.color ?: Avatar.COLOR_POR_DEFECTO,
    fondo = this?.fondo ?: Avatar.FONDO_POR_DEFECTO,
    foto = this?.foto?.let(ConfiguracionApi::urlCompleta)
)
