package com.example.electra.data.remote

// Forma exacta del JSON que envían y reciben los microservicios (ver Swagger de cada uno).
// Los campos que el servidor puede mandar en null son nullables: Gson no respeta la nulabilidad de Kotlin.

// ---------- usuarios-service ----------

data class RegistroDto(val nombre: String, val usuario: String, val contrasena: String, val rol: String)
data class LoginDto(val usuario: String, val contrasena: String)
data class RestablecerDto(val usuario: String, val nombre: String, val nueva: String)
data class CambiarContrasenaDto(val actual: String, val nueva: String)
data class NombreDto(val nombre: String)
data class RolDto(val rol: String)
data class AvatarDto(val color: String?, val fondo: String?, val foto: String?)

data class UsuarioDto(
    val usuario: String,
    val nombre: String,
    val rol: String,
    val avatar: AvatarDto?,
    val creadaEn: Long
)

// ---------- pruebas-service ----------

data class PruebaNuevaDto(
    val equipo: String,
    val tipoPrueba: String,
    val fecha: String,
    val hora: String,
    val medicion: String,
    val unidad: String,
    val resultado: String,
    val observaciones: String,
    val fotoUrl: String?,
    val latitud: Double?,
    val longitud: Double?,
    val creadoPor: String
)

data class PruebaDto(
    val id: Long,
    val equipo: String,
    val tipoPrueba: String,
    val fecha: String,
    val hora: String,
    val medicion: String,
    val unidad: String,
    val resultado: String,
    val observaciones: String?,
    val fotoUrl: String?,
    val latitud: Double?,
    val longitud: Double?,
    val creadoPor: String?,
    val creadoEn: Long?
)

// ---------- protocolos-service ----------

data class ProtocoloNuevoDto(
    val equipo: String,
    val fecha: String,
    val responsable: String,
    val observaciones: String,
    val creadoPor: String
)

data class ProtocoloDto(
    val id: Long,
    val numero: String,
    val equipo: String,
    val fecha: String,
    val estado: String,
    val responsable: String,
    val observaciones: String?,
    val creadoPor: String?,
    val creadoEn: Long?
)

data class EstadoDto(val estado: String, val usuario: String)
data class ObservacionesDto(val observaciones: String, val usuario: String)
data class NumeroDto(val numero: String)

// ---------- archivos-service ----------

data class ArchivoDto(val id: String, val ruta: String)
