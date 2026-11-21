package com.example.electra.data

import com.example.electra.model.Clima

/** Clima actual en unas coordenadas. null si no se pudo obtener (sin internet, API caída...). */
fun interface ClimaRepository {
    suspend fun actual(latitud: Double, longitud: Double): Clima?
}

/** Para pruebas unitarias y vistas previas: siempre responde el mismo clima (o ninguno). */
class ClimaFijo(private val clima: Clima? = null) : ClimaRepository {
    override suspend fun actual(latitud: Double, longitud: Double): Clima? = clima
}
