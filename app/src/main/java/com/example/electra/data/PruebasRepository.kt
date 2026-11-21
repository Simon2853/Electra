package com.example.electra.data

import com.example.electra.model.Prueba

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
