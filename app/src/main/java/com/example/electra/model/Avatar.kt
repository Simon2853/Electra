package com.example.electra.model

// Se guardan claves de texto y no R.drawable, porque los ids de recursos cambian entre compilaciones.
// "fondo" es el color de la cabecera del perfil.
data class Avatar(
    val icono: String = ICONO_POR_DEFECTO,
    val color: String = COLOR_POR_DEFECTO,
    val fondo: String = FONDO_POR_DEFECTO
) {
    companion object {
        const val ICONO_POR_DEFECTO = "persona"
        const val COLOR_POR_DEFECTO = "azul"
        const val FONDO_POR_DEFECTO = "blanco"
    }
}
