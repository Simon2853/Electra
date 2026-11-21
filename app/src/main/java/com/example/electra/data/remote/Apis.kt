package com.example.electra.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

// Un endpoint = una función. Retrofit genera la implementación; cada una devuelve Response<T>
// para poder revisar el código HTTP (201, 401, 409...) y no solo el cuerpo.
// Todas las rutas pasan por el API Gateway, que las reenvía al microservicio correspondiente.

/** usuarios-service: /api/auth y /api/usuarios */
interface UsuariosApi {

    @POST("api/auth/registro")
    suspend fun registrar(@Body datos: RegistroDto): Response<UsuarioDto>

    @POST("api/auth/login")
    suspend fun login(@Body datos: LoginDto): Response<UsuarioDto>

    @POST("api/auth/restablecer")
    suspend fun restablecer(@Body datos: RestablecerDto): Response<Unit>

    @GET("api/usuarios")
    suspend fun listar(): Response<List<UsuarioDto>>

    @GET("api/usuarios/{usuario}")
    suspend fun obtener(@Path("usuario") usuario: String): Response<UsuarioDto>

    @PUT("api/usuarios/{usuario}/nombre")
    suspend fun actualizarNombre(@Path("usuario") usuario: String, @Body datos: NombreDto): Response<UsuarioDto>

    @PUT("api/usuarios/{usuario}/contrasena")
    suspend fun cambiarContrasena(@Path("usuario") usuario: String, @Body datos: CambiarContrasenaDto): Response<Unit>

    @PUT("api/usuarios/{usuario}/avatar")
    suspend fun actualizarAvatar(@Path("usuario") usuario: String, @Body datos: AvatarDto): Response<UsuarioDto>

    @PUT("api/usuarios/{usuario}/rol")
    suspend fun actualizarRol(@Path("usuario") usuario: String, @Body datos: RolDto): Response<UsuarioDto>
}

/** pruebas-service: /api/pruebas. [usuario] = quien consulta; el servidor filtra según su rol. */
interface PruebasApi {

    @POST("api/pruebas")
    suspend fun registrar(@Body datos: PruebaNuevaDto): Response<PruebaDto>

    @GET("api/pruebas")
    suspend fun listar(@Query("usuario") usuario: String, @Query("equipo") equipo: String? = null): Response<List<PruebaDto>>

    @GET("api/pruebas/equipos")
    suspend fun equipos(@Query("usuario") usuario: String): Response<List<String>>

    @GET("api/pruebas/{id}")
    suspend fun obtener(@Path("id") id: Long, @Query("usuario") usuario: String): Response<PruebaDto>
}

/** protocolos-service: /api/protocolos. {numero} va sin "#" (p. ej. "000123"). */
interface ProtocolosApi {

    @POST("api/protocolos")
    suspend fun crear(@Body datos: ProtocoloNuevoDto): Response<ProtocoloDto>

    @GET("api/protocolos")
    suspend fun listar(@Query("usuario") usuario: String): Response<List<ProtocoloDto>>

    @GET("api/protocolos/siguiente-numero")
    suspend fun siguienteNumero(): Response<NumeroDto>

    @GET("api/protocolos/{numero}")
    suspend fun obtener(@Path("numero") numero: String, @Query("usuario") usuario: String): Response<ProtocoloDto>

    @PUT("api/protocolos/{numero}/estado")
    suspend fun cambiarEstado(@Path("numero") numero: String, @Body datos: EstadoDto): Response<ProtocoloDto>

    @PUT("api/protocolos/{numero}/observaciones")
    suspend fun actualizarObservaciones(@Path("numero") numero: String, @Body datos: ObservacionesDto): Response<ProtocoloDto>
}

/** archivos-service: /api/archivos (fotos de evidencia y de perfil). */
interface ArchivosApi {

    @Multipart
    @POST("api/archivos")
    suspend fun subir(
        @Part archivo: MultipartBody.Part,
        @Query("categoria") categoria: String,
        @Query("subidoPor") subidoPor: String
    ): Response<ArchivoDto>

    @DELETE("api/archivos/{id}")
    suspend fun eliminar(@Path("id") id: String, @Query("usuario") usuario: String): Response<Unit>
}
