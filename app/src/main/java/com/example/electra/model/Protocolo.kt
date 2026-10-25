package com.example.electra.model

data class Protocolo(
    val id: Long = 0,
    val numero: String,
    val equipo: String,
    val fecha: String,
    val estado: String,
    val responsable: String = "Técnico Electra",
    val observaciones: String = ""
)