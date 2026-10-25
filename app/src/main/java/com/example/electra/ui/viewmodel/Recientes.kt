package com.example.electra.ui.viewmodel

const val LIMITE_RECIENTES_POR_EQUIPO = 5

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
