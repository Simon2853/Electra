package com.example.electra.model

import java.util.Locale

/**
 * Condiciones ambientales en el lugar de la prueba, obtenidas de la API externa Open-Meteo.
 *
 * Importan en terreno porque la humedad y la lluvia bajan la resistencia de aislamiento medida:
 * una prueba "rechazada" con 90 % de humedad puede deberse al ambiente y no al equipo.
 *
 * @param codigo código WMO del estado del tiempo (0 = despejado, 61 = lluvia, 95 = tormenta...).
 */
data class Clima(val temperatura: Double, val humedad: Int, val codigo: Int) {

    val descripcion: String get() = descripcionDe(codigo)

    val humedadAlta: Boolean get() = humedad >= UMBRAL_HUMEDAD

    /** Llovizna, lluvia, chubascos o tormenta. */
    val conPrecipitacion: Boolean get() = codigo in 51..67 || codigo in 80..82 || codigo in 95..99

    /** Regla de negocio: avisar al técnico cuando el ambiente puede alterar la medición. */
    val condicionesAdversas: Boolean get() = humedadAlta || conPrecipitacion

    /** "18,4 °C · 62 % humedad · Parcialmente nublado" */
    val resumen: String
        get() = String.format(ES, "%.1f °C · %d %% humedad · %s", temperatura, humedad, descripcion)

    /** Texto que se agrega a las observaciones de la prueba si el técnico lo pide. */
    val textoObservacion: String get() = "Condiciones ambientales: $resumen (fuente: Open-Meteo)."

    companion object {
        /** Sobre este porcentaje de humedad relativa se recomienda dejar constancia en la prueba. */
        const val UMBRAL_HUMEDAD = 80

        private val ES: Locale = Locale.forLanguageTag("es-CL")

        /** Traduce los códigos WMO que entrega Open-Meteo a un texto corto en español. */
        fun descripcionDe(codigo: Int): String = when (codigo) {
            0 -> "Despejado"
            1, 2 -> "Parcialmente nublado"
            3 -> "Nublado"
            45, 48 -> "Niebla"
            in 51..57 -> "Llovizna"
            in 61..67, in 80..82 -> "Lluvia"
            in 71..77, 85, 86 -> "Nieve"
            in 95..99 -> "Tormenta"
            else -> "Sin datos"
        }
    }
}
