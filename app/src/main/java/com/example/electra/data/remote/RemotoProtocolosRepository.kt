package com.example.electra.data.remote

import com.example.electra.data.ProtocolosRepository
import com.example.electra.model.Protocolo

/**
 * Protocolos guardados en protocolos-service (antes estaban en SQLite).
 * El número correlativo lo asigna el servidor al guardar: el que muestra la app es solo referencial.
 */
class RemotoProtocolosRepository(
    private val api: ProtocolosApi,
    private val conexion: Conexion,
    private val usuarioActual: () -> String?
) : ProtocolosRepository {

    private fun sesion(): String? = usuarioActual()?.takeIf { it.isNotBlank() }

    override suspend fun listar(): List<Protocolo> {
        val usuario = sesion() ?: return emptyList()
        return conexion.llamar { api.listar(usuario) }.cuerpo().orEmpty().map { it.aProtocolo() }
    }

    override suspend fun buscar(numero: String): Protocolo? {
        val usuario = sesion() ?: return null
        return conexion.llamar { api.obtener(enUrl(numero), usuario) }.cuerpo()?.aProtocolo()
    }

    /** Vacío si no hay conexión: la pantalla muestra que el número se asigna al guardar. */
    override suspend fun siguienteNumero(): String = conexion.llamar { api.siguienteNumero() }.cuerpo()?.numero.orEmpty()

    override suspend fun insertar(protocolo: Protocolo): Boolean {
        val usuario = sesion() ?: protocolo.creadoPor ?: return false
        val datos = ProtocoloNuevoDto(
            equipo = protocolo.equipo.trim(),
            fecha = protocolo.fecha,
            responsable = protocolo.responsable.trim(),
            observaciones = protocolo.observaciones.trim(),
            creadoPor = usuario
        )
        return conexion.llamar { api.crear(datos) }.respondio(201)
    }

    override suspend fun cambiarEstado(numero: String, estado: String): Boolean {
        val usuario = sesion() ?: return false
        return conexion.llamar { api.cambiarEstado(enUrl(numero), EstadoDto(estado.trim(), usuario)) }.respondio(200)
    }

    override suspend fun actualizarObservaciones(numero: String, observaciones: String): Boolean {
        val usuario = sesion() ?: return false
        return conexion.llamar {
            api.actualizarObservaciones(enUrl(numero), ObservacionesDto(observaciones.trim(), usuario))
        }.respondio(200)
    }

    /** "#000123" -> "000123": el "#" en una URL marca el inicio de un ancla y no llegaría al servidor. */
    private fun enUrl(numero: String) = numero.trim().removePrefix("#")
}

internal fun ProtocoloDto.aProtocolo() = Protocolo(
    id = id,
    numero = numero,
    equipo = equipo,
    fecha = fecha,
    estado = estado,
    responsable = responsable,
    observaciones = observaciones.orEmpty(),
    creadoPor = creadoPor,
    creadoEn = creadoEn
)
