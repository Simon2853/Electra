package com.example.electra.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTRO = "registro"

    const val HOME = "home"
    const val EQUIPOS = "equipos"

    const val DETALLE_EQUIPO = "detalle_equipo"
    const val DETALLE_EQUIPO_PARAM = "detalle_equipo/{codigo}"

    const val REGISTRAR_PRUEBA = "registrar_prueba"
    const val REGISTRAR_PRUEBA_PARAM = "registrar_prueba/{codigo}"

    const val CONFIRMACION_PRUEBA = "confirmacion_prueba"

    const val PROTOCOLOS = "protocolos"
    const val PROTOCOLO = "protocolo"
    const val PROTOCOLO_PARAM = "protocolo/{numero}"
    const val CREAR_PROTOCOLO = "crear_protocolo"

    const val HISTORIAL_EQUIPO = "historial_equipo"
    const val HISTORIAL_EQUIPO_PARAM = "historial_equipo/{codigo}"

    const val DETALLE_PRUEBA = "detalle_prueba"
    const val DETALLE_PRUEBA_PARAM = "detalle_prueba/{idPrueba}"

    const val PERFIL = "perfil"

    fun detallePrueba(idPrueba: Long): String {
        return "detalle_prueba/$idPrueba"
    }

    fun protocolo(numero: String): String {
        return "protocolo/$numero"
    }
}