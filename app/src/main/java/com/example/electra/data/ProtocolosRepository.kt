package com.example.electra.data

import com.example.electra.model.Protocolo

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
