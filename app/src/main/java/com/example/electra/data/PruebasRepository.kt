package com.example.electra.data

import com.example.electra.model.Prueba
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Pruebas visibles para quien tiene la sesión (ver [Alcance]): las consultas solo devuelven lo
 * que le corresponde y [insertar] guarda como creador al usuario conectado.
 */
interface PruebasRepository {

    suspend fun insertar(prueba: Prueba): Long?
    suspend fun buscarPorId(id: Long): Prueba?
    suspend fun listarPorEquipo(codigoEquipo: String): List<Prueba>

    suspend fun listarTodas(): List<Prueba>
    suspend fun equiposConRegistros(): List<String>
}

class SqlitePruebasRepository(
    private val db: PruebaDbHelper,
    /** Se consulta en cada operación, así cambia con la sesión sin recrear el repositorio. */
    private val alcance: suspend () -> Alcance = { Alcance.TODO },
    private val reloj: () -> Long = System::currentTimeMillis,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : PruebasRepository {

    override suspend fun insertar(prueba: Prueba): Long? {
        val creador = alcance().usuario ?: prueba.creadoPor
        val ahora = reloj()
        return withContext(io) {
            db.insertarPrueba(prueba.copy(creadoPor = creador, creadoEn = ahora)).takeIf { it != -1L }
        }
    }

    override suspend fun buscarPorId(id: Long): Prueba? {
        val alcance = alcance()
        return withContext(io) { db.buscarPruebaPorId(id) }?.takeIf { alcance.puedeVer(it.creadoPor, it.creadoEn) }
    }

    override suspend fun listarPorEquipo(codigoEquipo: String): List<Prueba> {
        val alcance = alcance()
        return withContext(io) { db.listarPruebasPorEquipo(codigoEquipo, alcance.creador, alcance.creadosDesde) }
    }

    override suspend fun listarTodas(): List<Prueba> {
        val alcance = alcance()
        return withContext(io) { db.listarPruebas(alcance.creador, alcance.creadosDesde) }
    }

    override suspend fun equiposConRegistros(): List<String> {
        val alcance = alcance()
        return withContext(io) { db.obtenerEquiposConRegistros(alcance.creador, alcance.creadosDesde) }
    }
}
