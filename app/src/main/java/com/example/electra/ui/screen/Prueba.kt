package com.example.electra.ui.screen

import androidx.compose.runtime.mutableStateListOf

data class Prueba(
    val equipo: String,
    val tipoPrueba: String,
    val fecha: String,
    val hora: String,
    val medicion: String,
    val unidad: String,
    val resultado: String,
    val observaciones: String
)

object RegistroPruebas {
    val pruebas = mutableStateListOf<Prueba>()
}