package com.example.electra.ui.viewmodel

import com.example.electra.model.Protocolo
import com.example.electra.model.Prueba

fun prueba(id: Long = 0, equipo: String = "TR-001", medicion: String = "1200") = Prueba(
    id = id,
    equipo = equipo,
    tipoPrueba = "Resistencia de aislamiento",
    fecha = "23/10/2026",
    hora = "10:30",
    medicion = medicion,
    unidad = "MΩ",
    resultado = "Aprobado",
    observaciones = ""
)

fun protocolo(id: Long, numero: String, estado: String = "Registrado en terreno") = Protocolo(
    id = id,
    numero = numero,
    equipo = "TR-001",
    fecha = "01/10/2026",
    estado = estado,
    responsable = "Juan Pérez"
)
