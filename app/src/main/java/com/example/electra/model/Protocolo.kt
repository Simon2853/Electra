package com.example.electra.model

data class Protocolo(
    val id: Long = 0,
    val numero: String,
    val equipo: String,
    val fecha: String,
    val estado: String, // "Registrado en terreno" o "Formalizado"
    val responsable: String = "Técnico Electra",
    val observaciones: String = ""
)