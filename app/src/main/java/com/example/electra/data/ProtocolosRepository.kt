package com.example.electra.data

import com.example.electra.model.Protocolo
import com.example.electra.model.siguienteNumero
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Protocolos visibles para quien tiene la sesión (ver [Alcance]): las consultas solo devuelven lo
 * que le corresponde, [insertar] guarda como creador al usuario conectado y no se puede modificar
 * un protocolo que no se puede ver.
 */
interface ProtocolosRepository {
    suspend fun listar(): List<Protocolo>

    suspend fun buscar(numero: String): Protocolo?

    /** Siguiente número libre, contando los protocolos de TODOS los usuarios (el número es único). */
    suspend fun siguienteNumero(): String

    suspend fun insertar(protocolo: Protocolo): Boolean
    suspend fun cambiarEstado(numero: String, estado: String): Boolean
    suspend fun actualizarObservaciones(numero: String, observaciones: String): Boolean
}

class SqliteProtocolosRepository(
    private val db: PruebaDbHelper,
    /** Se consulta en cada operación, así cambia con la sesión sin recrear el repositorio. */
    private val alcance: suspend () -> Alcance = { Alcance.TODO },
    private val reloj: () -> Long = System::currentTimeMillis,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : ProtocolosRepository {

    override suspend fun listar(): List<Protocolo> {
        val alcance = alcance()
        return withContext(io) { db.listarProtocolos(alcance.creador, alcance.creadosDesde) }
    }

    override suspend fun buscar(numero: String): Protocolo? {
        val alcance = alcance()
        return withContext(io) { db.buscarProtocoloPorNumero(numero) }?.takeIf { alcance.puedeVer(it.creadoPor, it.creadoEn) }
    }

    override suspend fun siguienteNumero(): String = withContext(io) {
        siguienteNumero(db.listarProtocolos())
    }

    override suspend fun insertar(protocolo: Protocolo): Boolean {
        val creador = alcance().usuario ?: protocolo.creadoPor
        val ahora = reloj()
        return withContext(io) {
            db.insertarProtocolo(protocolo.copy(creadoPor = creador, creadoEn = ahora)) != -1L
        }
    }

    override suspend fun cambiarEstado(numero: String, estado: String): Boolean {
        if (buscar(numero) == null) return false
        return withContext(io) { db.cambiarEstadoProtocolo(numero, estado) }
    }

    override suspend fun actualizarObservaciones(numero: String, observaciones: String): Boolean {
        if (buscar(numero) == null) return false
        return withContext(io) { db.actualizarObservacionesProtocolo(numero, observaciones) }
    }
}
