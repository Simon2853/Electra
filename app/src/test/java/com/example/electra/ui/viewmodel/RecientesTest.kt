package com.example.electra.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class RecientesTest {

    private data class Registro(val id: Int, val equipo: String)

    @Test
    fun conservaLosPrimerosDeCadaEquipoEnElMismoOrden() {
        val lista = listOf(
            Registro(9, "TR-001"), Registro(8, "RL-004"), Registro(7, "TR-001"),
            Registro(6, "TR-001"), Registro(5, "RL-004"), Registro(4, "RL-004")
        )

        val resultado = lista.ultimosPorEquipo(limite = 2) { it.equipo }

        assertEquals(listOf(9, 8, 7, 5), resultado.map { it.id })
    }

    @Test
    fun ignoraEspaciosEnElCodigoDelEquipo() {
        val lista = listOf(Registro(3, "TR-001"), Registro(2, " TR-001 "), Registro(1, "TR-001"))

        assertEquals(listOf(3, 2), lista.ultimosPorEquipo(limite = 2) { it.equipo }.map { it.id })
    }

    @Test
    fun listasCortasQuedanIgual() {
        val lista = listOf(Registro(2, "A"), Registro(1, "B"))
        assertEquals(lista, lista.ultimosPorEquipo { it.equipo })
    }
}
