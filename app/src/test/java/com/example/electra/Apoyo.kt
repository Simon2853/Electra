package com.example.electra

import com.example.electra.data.remote.ArchivoDto
import com.example.electra.data.remote.ArchivosApi
import com.example.electra.data.remote.AvatarDto
import com.example.electra.data.remote.CambiarContrasenaDto
import com.example.electra.data.remote.EstadoDto
import com.example.electra.data.remote.LoginDto
import com.example.electra.data.remote.NombreDto
import com.example.electra.data.remote.NumeroDto
import com.example.electra.data.remote.OpenMeteoApi
import com.example.electra.data.remote.RespuestaOpenMeteo
import com.example.electra.data.remote.ObservacionesDto
import com.example.electra.data.remote.ProtocoloDto
import com.example.electra.data.remote.ProtocoloNuevoDto
import com.example.electra.data.remote.ProtocolosApi
import com.example.electra.data.remote.PruebaDto
import com.example.electra.data.remote.PruebaNuevaDto
import com.example.electra.data.remote.PruebasApi
import com.example.electra.data.remote.RegistroDto
import com.example.electra.data.remote.RestablecerDto
import com.example.electra.data.remote.RolDto
import com.example.electra.data.remote.UsuarioDto
import com.example.electra.data.remote.UsuariosApi
import com.example.electra.model.Protocolo
import com.example.electra.model.Prueba
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.Response
import java.io.IOException

// =============================================================================================
// APOYO PARA LAS PRUEBAS (no contiene pruebas: no tiene @Test).
// Lo usan los 4 archivos de pruebas: AutenticacionTest, PerfilTest, PruebasElectricasTest y ProtocolosTest.
// =============================================================================================

/** Hace que viewModelScope funcione en las pruebas: reemplaza el hilo principal de Android. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

// ---------- datos de ejemplo ----------

fun prueba(id: Long = 0, equipo: String = "TR-001", medicion: String = "1200") = Prueba(
    id = id,
    equipo = equipo,
    tipoPrueba = "Resistencia de aislamiento",
    fecha = "23/10/2026",
    hora = "10:30",
    medicion = medicion,
    unidad = "MΩ",
    resultado = "Aprobado",
    observaciones = ""
)

fun protocolo(id: Long, numero: String, estado: String = "Registrado en terreno", equipo: String = "TR-001") = Protocolo(
    id = id,
    numero = numero,
    equipo = equipo,
    fecha = "01/10/2026",
    estado = estado,
    responsable = "Juan Pérez"
)

// ---------- respuestas HTTP armadas a mano ----------

fun <T> respuesta(codigo: Int, cuerpo: T): Response<T> = Response.success(codigo, cuerpo)

fun <T> errorHttp(codigo: Int): Response<T> = Response.error(codigo, "{}".toResponseBody(null))

/** Lo que lanza Retrofit cuando no hay red o el servidor no responde. */
fun sinRed(): Nothing = throw IOException("Sin conexión")

// =============================================================================================
// SERVIDOR FALSO: implementa las mismas interfaces Retrofit que usa la app, pero en memoria.
// Responde con los mismos códigos HTTP que los microservicios reales (201, 401, 404, 409...),
// así se prueban los repositorios remotos sin levantar el backend.
// Con [caido] = true, cada llamada se comporta como si no hubiera red.
// =============================================================================================

class UsuariosApiFalsa : UsuariosApi {
    var caido = false
    val cuentas = mutableMapOf<String, UsuarioDto>()
    val claves = mutableMapOf<String, String>()
    var llamadasObtener = 0

    fun agregar(usuario: String, nombre: String, clave: String, rol: String = "tecnico", creadaEn: Long = 1_000) {
        cuentas[usuario] = UsuarioDto(usuario, nombre, rol, AvatarDto(null, null, null), creadaEn)
        claves[usuario] = clave
    }

    override suspend fun registrar(datos: RegistroDto): Response<UsuarioDto> {
        if (caido) sinRed()
        if (datos.usuario in cuentas) return errorHttp(409)
        agregar(datos.usuario, datos.nombre, datos.contrasena, datos.rol)
        return respuesta(201, cuentas.getValue(datos.usuario))
    }

    override suspend fun login(datos: LoginDto): Response<UsuarioDto> {
        if (caido) sinRed()
        return if (claves[datos.usuario] == datos.contrasena) respuesta(200, cuentas.getValue(datos.usuario)) else errorHttp(401)
    }

    override suspend fun restablecer(datos: RestablecerDto): Response<Unit> {
        if (caido) sinRed()
        val cuenta = cuentas[datos.usuario] ?: return errorHttp(404)
        if (!cuenta.nombre.equals(datos.nombre, ignoreCase = true)) return errorHttp(400)
        claves[datos.usuario] = datos.nueva
        return respuesta(204, Unit)
    }

    override suspend fun listar(): Response<List<UsuarioDto>> {
        if (caido) sinRed()
        return respuesta(200, cuentas.values.sortedBy { it.nombre })
    }

    override suspend fun obtener(usuario: String): Response<UsuarioDto> {
        if (caido) sinRed()
        llamadasObtener++
        return cuentas[usuario]?.let { respuesta(200, it) } ?: errorHttp(404)
    }

    override suspend fun actualizarNombre(usuario: String, datos: NombreDto): Response<UsuarioDto> =
        modificar(usuario) { it.copy(nombre = datos.nombre) }

    override suspend fun cambiarContrasena(usuario: String, datos: CambiarContrasenaDto): Response<Unit> {
        if (caido) sinRed()
        if (claves[usuario] != datos.actual) return errorHttp(401)
        claves[usuario] = datos.nueva
        return respuesta(204, Unit)
    }

    override suspend fun actualizarAvatar(usuario: String, datos: AvatarDto): Response<UsuarioDto> =
        modificar(usuario) { it.copy(avatar = datos) }

    override suspend fun actualizarRol(usuario: String, datos: RolDto): Response<UsuarioDto> =
        modificar(usuario) { it.copy(rol = datos.rol) }

    private fun modificar(usuario: String, cambio: (UsuarioDto) -> UsuarioDto): Response<UsuarioDto> {
        if (caido) sinRed()
        val cuenta = cuentas[usuario] ?: return errorHttp(404)
        return respuesta(200, cambio(cuenta).also { cuentas[usuario] = it })
    }
}

class ArchivosApiFalsa : ArchivosApi {
    var caido = false
    var subidas = 0
    val eliminados = mutableListOf<String>()

    override suspend fun subir(archivo: MultipartBody.Part, categoria: String, subidoPor: String): Response<ArchivoDto> {
        if (caido) sinRed()
        subidas++
        return respuesta(201, ArchivoDto("foto$subidas", "/api/archivos/foto$subidas"))
    }

    override suspend fun eliminar(id: String, usuario: String): Response<Unit> {
        if (caido) sinRed()
        eliminados += id
        return respuesta(204, Unit)
    }
}

class PruebasApiFalsa : PruebasApi {
    var caido = false
    val guardadas = mutableListOf<PruebaDto>()
    var ultimaRecibida: PruebaNuevaDto? = null

    override suspend fun registrar(datos: PruebaNuevaDto): Response<PruebaDto> {
        if (caido) sinRed()
        ultimaRecibida = datos
        val creada = PruebaDto(
            id = guardadas.size + 1L, equipo = datos.equipo, tipoPrueba = datos.tipoPrueba, fecha = datos.fecha,
            hora = datos.hora, medicion = datos.medicion, unidad = datos.unidad, resultado = datos.resultado,
            observaciones = datos.observaciones, fotoUrl = datos.fotoUrl, latitud = datos.latitud,
            longitud = datos.longitud, creadoPor = datos.creadoPor, creadoEn = 5_000
        )
        guardadas += creada
        return respuesta(201, creada)
    }

    override suspend fun listar(usuario: String, equipo: String?): Response<List<PruebaDto>> {
        if (caido) sinRed()
        return respuesta(200, guardadas.filter { equipo == null || it.equipo == equipo }.reversed())
    }

    override suspend fun equipos(usuario: String): Response<List<String>> {
        if (caido) sinRed()
        return respuesta(200, guardadas.map { it.equipo }.distinct())
    }

    override suspend fun obtener(id: Long, usuario: String): Response<PruebaDto> {
        if (caido) sinRed()
        return guardadas.firstOrNull { it.id == id }?.let { respuesta(200, it) } ?: errorHttp(404)
    }
}

class ProtocolosApiFalsa : ProtocolosApi {
    var caido = false
    val guardados = mutableListOf<ProtocoloDto>()
    var numerosPedidos = mutableListOf<String>()

    override suspend fun crear(datos: ProtocoloNuevoDto): Response<ProtocoloDto> {
        if (caido) sinRed()
        val id = guardados.size + 1L
        val creado = ProtocoloDto(
            id, "#" + id.toString().padStart(6, '0'), datos.equipo, datos.fecha, "Registrado en terreno",
            datos.responsable, datos.observaciones, datos.creadoPor, 5_000
        )
        guardados += creado
        return respuesta(201, creado)
    }

    override suspend fun listar(usuario: String): Response<List<ProtocoloDto>> {
        if (caido) sinRed()
        return respuesta(200, guardados.reversed())
    }

    override suspend fun siguienteNumero(): Response<NumeroDto> {
        if (caido) sinRed()
        return respuesta(200, NumeroDto("#" + (guardados.size + 1).toString().padStart(6, '0')))
    }

    override suspend fun obtener(numero: String, usuario: String): Response<ProtocoloDto> {
        if (caido) sinRed()
        numerosPedidos += numero
        return buscar(numero)?.let { respuesta(200, it) } ?: errorHttp(404)
    }

    override suspend fun cambiarEstado(numero: String, datos: EstadoDto): Response<ProtocoloDto> {
        if (caido) sinRed()
        val actual = buscar(numero) ?: return errorHttp(404)
        if (actual.estado == "Formalizado") return errorHttp(409)
        return respuesta(200, reemplazar(actual, actual.copy(estado = datos.estado)))
    }

    override suspend fun actualizarObservaciones(numero: String, datos: ObservacionesDto): Response<ProtocoloDto> {
        if (caido) sinRed()
        val actual = buscar(numero) ?: return errorHttp(404)
        if (actual.estado == "Formalizado") return errorHttp(409)
        return respuesta(200, reemplazar(actual, actual.copy(observaciones = datos.observaciones)))
    }

    private fun buscar(numero: String) = guardados.firstOrNull { it.numero == "#$numero" }

    private fun reemplazar(actual: ProtocoloDto, nuevo: ProtocoloDto): ProtocoloDto {
        guardados[guardados.indexOf(actual)] = nuevo
        return nuevo
    }
}

/** Open-Meteo falsa: responde lo que indique [respuesta] (o lanza sinRed()). */
class OpenMeteoFalsa(private val respuesta: () -> Response<RespuestaOpenMeteo>) : OpenMeteoApi {
    override suspend fun actual(latitud: Double, longitud: Double, campos: String): Response<RespuestaOpenMeteo> = respuesta()
}
