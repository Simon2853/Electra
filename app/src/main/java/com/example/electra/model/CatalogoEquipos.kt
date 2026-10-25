package com.example.electra.model

data class Equipo(
    val codigo: String,
    val tipo: String,
    val cliente: String,
    val instalacion: String
)

object CatalogoEquipos {

    val EQUIPOS = listOf(
        Equipo("TR-001", "Transformador", "Empresa Eléctrica Norte", "Subestación Norte"),
        Equipo("TR-002", "Transformador", "Industrias del Pacífico", "Planta Industrial"),
        Equipo("RL-004", "Relé de protección", "Empresa Eléctrica Norte", "Subestación Norte"),
        Equipo("CE-008", "Celda eléctrica", "Induztek Ingeniería", "Subestación Central"),
        Equipo("CB-012", "Cable", "Industrias del Pacífico", "Planta Industrial")
    )

    val CODIGOS_POR_DEFECTO = listOf("TR-001", "TR-002", "RL-004", "CE-008")

    val CODIGOS = EQUIPOS.map { it.codigo }

    val TIPOS = EQUIPOS.map { it.tipo }.distinct()

    fun buscar(codigo: String): Equipo? = EQUIPOS.firstOrNull { it.codigo.equals(codigo.trim(), ignoreCase = true) }

    fun filtrar(texto: String, tipo: String? = null): List<Equipo> {
        val busqueda = texto.trim()
        return EQUIPOS.filter { equipo ->
            val coincideTexto = busqueda.isEmpty() ||
                    listOf(equipo.codigo, equipo.cliente, equipo.instalacion).any { it.contains(busqueda, ignoreCase = true) }
            coincideTexto && (tipo == null || equipo.tipo == tipo)
        }
    }
}

object OpcionesPrueba {
    val TIPOS = listOf(
        "Resistencia de aislamiento",
        "Resistencia de puesta a tierra",
        "Relación de transformación",
        "Prueba de continuidad"
    )
    val UNIDADES = listOf("MΩ", "Ω", "kΩ", "V", "A")
    const val APROBADO = "Aprobado"
    const val RECHAZADO = "Rechazado"
}

object EstadosProtocolo {
    const val EN_TERRENO = "Registrado en terreno"
    const val FORMALIZADO = "Formalizado"
    val TODOS = listOf(EN_TERRENO, FORMALIZADO)
}
