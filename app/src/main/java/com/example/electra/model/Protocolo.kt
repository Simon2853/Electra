package com.example.electra.model

data class Protocolo(
    val id: Long = 0,
    val numero: String,
    val equipo: String,
    val fecha: String,
    val estado: String,
    val responsable: String = "Técnico Electra",
    val observaciones: String = "",
    /** Usuario (login) que creó el protocolo; null en registros anteriores a esta columna. */
    val creadoPor: String? = null,
    /** Momento en que se guardó (epoch en milisegundos); null en registros anteriores a esta columna. */
    val creadoEn: Long? = null
)

/** Número correlativo siguiente al mayor de [existentes], con el formato "#000123". */
fun siguienteNumero(existentes: List<Protocolo>): String {
    val mayor = existentes.mapNotNull { it.numero.removePrefix("#").toIntOrNull() }.maxOrNull() ?: 0
    return "#" + (mayor + 1).toString().padStart(6, '0')
}