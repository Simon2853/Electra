package com.example.electra.data.remote

import com.example.electra.data.PruebasRepository
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.Prueba

/**
 * Pruebas guardadas en pruebas-service (antes estaban en SQLite). Qué pruebas ve cada uno lo decide
 * el servidor según el rol de [usuarioActual]; sin sesión no se consulta nada.
 */
class RemotoPruebasRepository(
    private val api: PruebasApi,
    private val conexion: Conexion,
    private val archivos: ArchivosRemoto,
    private val usuarioActual: () -> String?,
    private val eliminarLocal: (String) -> Unit = {}
) : PruebasRepository {

    private fun sesion(): String? = usuarioActual()?.takeIf { it.isNotBlank() }

    /** Sube primero la foto de evidencia (si hay) y después registra la prueba con la ruta de la foto. */
    override suspend fun insertar(prueba: Prueba): Long? {
        val usuario = sesion() ?: prueba.creadoPor ?: return null
        val foto = when (val preparada = archivos.preparar(prueba.fotoUri, "evidencia", usuario)) {
            FotoPreparada.SinFoto -> null
            FotoPreparada.Fallo -> return null // la foto local se conserva para reintentar
            is FotoPreparada.Lista -> preparada
        }
        val creada = conexion.llamar { api.registrar(prueba.aNuevaDto(usuario, foto?.ruta)) }.cuerpo()
            ?: return null
        foto?.subidaDesde?.let(eliminarLocal)
        return creada.id
    }

    override suspend fun buscarPorId(id: Long): Prueba? {
        val usuario = sesion() ?: return null
        return conexion.llamar { api.obtener(id, usuario) }.cuerpo()?.aPrueba()
    }

    override suspend fun listarPorEquipo(codigoEquipo: String): List<Prueba> {
        val usuario = sesion() ?: return emptyList()
        return conexion.llamar { api.listar(usuario, codigoEquipo.trim()) }.cuerpo().orEmpty().map { it.aPrueba() }
    }

    override suspend fun listarTodas(): List<Prueba> {
        val usuario = sesion() ?: return emptyList()
        return conexion.llamar { api.listar(usuario) }.cuerpo().orEmpty().map { it.aPrueba() }
    }

    /** Sin registros (o sin conexión) se muestran los equipos del catálogo, como antes. */
    override suspend fun equiposConRegistros(): List<String> {
        val usuario = sesion() ?: return CatalogoEquipos.CODIGOS_POR_DEFECTO
        return conexion.llamar { api.equipos(usuario) }.cuerpo().orEmpty().ifEmpty { CatalogoEquipos.CODIGOS_POR_DEFECTO }
    }
}

internal fun Prueba.aNuevaDto(usuario: String, rutaFoto: String?) = PruebaNuevaDto(
    equipo = equipo.trim(),
    tipoPrueba = tipoPrueba,
    fecha = fecha,
    hora = hora,
    medicion = medicion.trim().replace(',', '.'),
    unidad = unidad,
    resultado = resultado,
    observaciones = observaciones.trim(),
    fotoUrl = rutaFoto,
    latitud = latitud,
    longitud = longitud,
    creadoPor = usuario
)

internal fun PruebaDto.aPrueba() = Prueba(
    id = id,
    equipo = equipo,
    tipoPrueba = tipoPrueba,
    fecha = fecha,
    hora = hora,
    medicion = medicion,
    unidad = unidad,
    resultado = resultado,
    observaciones = observaciones.orEmpty(),
    fotoUri = fotoUrl?.let(ConfiguracionApi::urlCompleta),
    latitud = latitud,
    longitud = longitud,
    creadoPor = creadoPor,
    creadoEn = creadoEn
)
