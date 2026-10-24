package com.example.electra.ui.viewmodel

/** Cantidad de registros recientes que se muestran por equipo en historial y protocolos. */
const val LIMITE_RECIENTES_POR_EQUIPO = 5

/**
 * Conserva los primeros [limite] elementos de cada equipo, respetando el orden de la lista.
 * Se espera una lista ya ordenada de más reciente a más antiguo.
 */
fun <T> List<T>.ultimosPorEquipo(
    limite: Int = LIMITE_RECIENTES_POR_EQUIPO,
    equipo: (T) -> String
): List<T> {
    val vistos = mutableMapOf<String, Int>()
    return filter { elemento ->
        val clave = equipo(elemento).trim()
        val cantidad = vistos.getOrDefault(clave, 0)
        vistos[clave] = cantidad + 1
        cantidad < limite
    }
}
