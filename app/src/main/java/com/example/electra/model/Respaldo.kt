package com.example.electra.model

import java.util.Locale


object Respaldo {

    fun dePrueba(prueba: Prueba): String = buildString {
        appendLine("RESPALDO DE PRUEBA ELÉCTRICA - ELECTRA")
        appendLine("Induztek Ingeniería SpA")
        appendLine()
        appendLine("Equipo: ${prueba.equipo}")
        CatalogoEquipos.buscar(prueba.equipo)?.let {
            appendLine("Tipo: ${it.tipo}")
            appendLine("Instalación: ${it.instalacion} (${it.cliente})")
        }
        appendLine("Prueba: ${prueba.tipoPrueba}")
        appendLine("Fecha y hora: ${prueba.fecha} ${prueba.hora}")
        appendLine("Medición: ${prueba.medicion} ${prueba.unidad}")
        appendLine("Resultado: ${prueba.resultado}")
        if (prueba.observaciones.isNotBlank()) appendLine("Observaciones: ${prueba.observaciones}")
        coordenadas(prueba)?.let { appendLine("Ubicación GPS: $it") }
        appendLine()
        append("Estado: registrado en terreno (pendiente de formalización).")
    }

    fun deProtocolo(protocolo: Protocolo, pruebas: List<Prueba>): String = buildString {
        appendLine("PROTOCOLO ${protocolo.numero} - ELECTRA")
        appendLine("Induztek Ingeniería SpA")
        appendLine()
        appendLine("Equipo: ${protocolo.equipo}")
        CatalogoEquipos.buscar(protocolo.equipo)?.let {
            appendLine("Instalación: ${it.instalacion} (${it.cliente})")
        }
        appendLine("Fecha: ${protocolo.fecha}")
        appendLine("Responsable: ${protocolo.responsable}")
        appendLine("Estado: ${protocolo.estado}")
        if (protocolo.observaciones.isNotBlank()) appendLine("Observaciones: ${protocolo.observaciones}")
        if (pruebas.isNotEmpty()) {
            appendLine()
            appendLine("Pruebas registradas del equipo:")
            pruebas.forEach {
                appendLine("- ${it.fecha} ${it.hora} | ${it.tipoPrueba}: ${it.medicion} ${it.unidad} (${it.resultado})")
            }
        }
    }

    fun coordenadas(prueba: Prueba): String? {
        val lat = prueba.latitud ?: return null
        val lon = prueba.longitud ?: return null
        return String.format(Locale.US, "%.5f, %.5f", lat, lon)
    }
}
