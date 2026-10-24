package com.example.electra.model

data class Prueba(
    val id: Long = 0,
    val equipo: String,
    val tipoPrueba: String,
    val fecha: String,
    val hora: String,
    val medicion: String,
    val unidad: String,
    val resultado: String,
    val observaciones: String
)
