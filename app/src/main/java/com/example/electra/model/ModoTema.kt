package com.example.electra.model

/** Apariencia elegida por el usuario. "clave" es lo que se guarda en el almacenamiento: no cambiarla. */
enum class ModoTema(val clave: String, val nombre: String) {
    SISTEMA("sistema", "Sistema"),
    CLARO("claro", "Claro"),
    OSCURO("oscuro", "Oscuro");

    /** Si corresponde usar la paleta oscura, dado si el sistema está en modo oscuro. */
    fun esOscuro(sistemaOscuro: Boolean): Boolean = when (this) {
        SISTEMA -> sistemaOscuro
        CLARO -> false
        OSCURO -> true
    }

    companion object {
        val POR_DEFECTO = SISTEMA

        fun desdeClave(clave: String?): ModoTema = entries.firstOrNull { it.clave == clave } ?: POR_DEFECTO
    }
}
