package com.example.electra.model

// Se guardan claves de texto y no Color, para poder cambiar los tonos sin perder lo elegido.
// "color" es el círculo de las iniciales, "fondo" el de la cabecera del perfil y
// "foto" la Uri (content:// del FileProvider propio) de la foto de perfil, si el técnico subió una.
data class Avatar(
    val color: String = COLOR_POR_DEFECTO,
    val fondo: String = FONDO_POR_DEFECTO,
    val foto: String? = null
) {
    companion object {
        const val COLOR_POR_DEFECTO = "azul"
        const val FONDO_POR_DEFECTO = "blanco"

        /** "Ana María Torres" -> "AT"; "ana" -> "A"; vacío -> "?". */
        fun iniciales(nombre: String): String {
            val palabras = nombre.trim().split(Regex("""\s+""")).filter { it.isNotEmpty() }
            val letras = when {
                palabras.isEmpty() -> return "?"
                palabras.size == 1 -> palabras.first().take(1)
                else -> palabras.first().take(1) + palabras.last().take(1)
            }
            return letras.uppercase()
        }
    }
}
