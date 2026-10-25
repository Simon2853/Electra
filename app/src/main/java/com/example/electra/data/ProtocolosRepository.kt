package com.example.electra.data

import com.example.electra.model.Protocolo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ProtocolosRepository {
    suspend fun listar(): List<Protocolo>


    suspend fun buscar(numero: String): Protocolo?

    suspend fun insertar(protocolo: Protocolo): Boolean
    suspend fun cambiarEstado(numero: String, estado: String): Boolean
    suspend fun actualizarObservaciones(numero: String, observaciones: String): Boolean
}

class SqliteProtocolosRepository(
    private val db: PruebaDbHelper,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : ProtocolosRepository {

    override suspend fun listar(): List<Protocolo> = withContext(io) {
        db.listarProtocolos()
    }

    override suspend fun buscar(numero: String): Protocolo? = withContext(io) {
        db.buscarProtocoloPorNumero(numero)
    }

    override suspend fun insertar(protocolo: Protocolo): Boolean = withContext(io) {
        db.insertarProtocolo(protocolo) != -1L
    }

    override suspend fun cambiarEstado(numero: String, estado: String): Boolean = withContext(io) {
        db.cambiarEstadoProtocolo(numero, estado)
    }

    override suspend fun actualizarObservaciones(numero: String, observaciones: String): Boolean =
        withContext(io) {
            db.actualizarObservacionesProtocolo(numero, observaciones)
        }
}
