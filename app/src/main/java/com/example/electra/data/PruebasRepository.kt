package com.example.electra.data

import com.example.electra.model.Prueba
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PruebasRepository {
    /** Devuelve el id asignado, o null si no se pudo guardar. */
    suspend fun insertar(prueba: Prueba): Long?
    suspend fun buscarPorId(id: Long): Prueba?
    suspend fun listarPorEquipo(codigoEquipo: String): List<Prueba>
    suspend fun equiposConRegistros(): List<String>
}

class SqlitePruebasRepository(
    private val db: PruebaDbHelper,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : PruebasRepository {

    override suspend fun insertar(prueba: Prueba): Long? = withContext(io) {
        db.insertarPrueba(prueba).takeIf { it != -1L }
    }

    override suspend fun buscarPorId(id: Long): Prueba? = withContext(io) {
        db.buscarPruebaPorId(id)
    }

    override suspend fun listarPorEquipo(codigoEquipo: String): List<Prueba> = withContext(io) {
        db.listarPruebasPorEquipo(codigoEquipo)
    }

    override suspend fun equiposConRegistros(): List<String> = withContext(io) {
        db.obtenerEquiposConRegistros()
    }
}
