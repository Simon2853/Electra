package com.example.electra.validation

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Validaciones {

    const val MAX_OBSERVACIONES = 300
    const val MAX_MEDICION = 1_000_000.0

    private val SOLO_LETRAS = Regex("^[\\p{L} .'-]+$")
    private val USUARIO_VALIDO = Regex("^[A-Za-z0-9._-]+$")
    private val NUMERO_PROTOCOLO = Regex("^#?\\d{6}$")


    fun usuarioLogin(usuario: String): String? =
        if (usuario.isBlank()) "Ingresa tu usuario." else null

    fun contrasenaLogin(contrasena: String): String? =
        if (contrasena.isEmpty()) "Ingresa tu contraseña." else null

    fun nombre(nombre: String): String? {
        val limpio = nombre.trim()
        return when {
            limpio.isEmpty() -> "Ingresa tu nombre completo."
            limpio.length < 3 -> "El nombre debe tener al menos 3 letras."
            !SOLO_LETRAS.matches(limpio) -> "El nombre solo puede contener letras."
            else -> null
        }
    }

    fun usuarioRegistro(usuario: String): String? {
        val limpio = usuario.trim()
        return when {
            limpio.isEmpty() -> "Ingresa un nombre de usuario."
            limpio.contains(" ") -> "El usuario no debe contener espacios."
            limpio.length < 4 -> "El usuario debe tener al menos 4 caracteres."
            limpio.length > 20 -> "El usuario no puede superar los 20 caracteres."
            !USUARIO_VALIDO.matches(limpio) -> "Usa solo letras, números, punto, guion o guion bajo."
            else -> null
        }
    }

    fun contrasenaRegistro(nombre: String, usuario: String, contrasena: String): String? {
        if (contrasena.isEmpty()) return "Ingresa una contraseña."
        if (contrasena.length < 8) return "La clave debe tener al menos 8 caracteres."
        if (contrasena.length > 20) return "La clave no puede superar los 20 caracteres."
        if (contrasena.none { it.isUpperCase() }) return "La clave debe incluir al menos una letra mayúscula."
        if (contrasena.none { it.isLowerCase() }) return "La clave debe incluir al menos una letra minúscula."
        if (contrasena.none { it.isDigit() }) return "La clave debe incluir al menos un número."
        if (contrasena.none { !it.isLetterOrDigit() }) {
            return "La clave debe incluir un carácter especial: ! @ # $ % & * ?"
        }

        val normalizada = contrasena.lowercase().filter { it.isLetterOrDigit() }
        if ("electra" in normalizada) return "No incluyas el nombre de la aplicación."

        val datosPersonales = (nombre + " " + usuario)
            .lowercase()
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length >= 3 }
        if (datosPersonales.any { it in normalizada }) return "No incluyas tu nombre ni tu usuario."

        return null
    }

    fun confirmacion(contrasena: String, confirmar: String): String? = when {
        confirmar.isEmpty() -> "Confirma tu contraseña."
        contrasena != confirmar -> "Las contraseñas no coinciden."
        else -> null
    }

    fun requisitosContrasena(contrasena: String): List<Pair<String, Boolean>> = listOf(
        "Entre 8 y 20 caracteres" to (contrasena.length in 8..20),
        "Al menos una letra mayúscula" to contrasena.any { it.isUpperCase() },
        "Al menos una letra minúscula" to contrasena.any { it.isLowerCase() },
        "Al menos un número" to contrasena.any { it.isDigit() },
        "Al menos un carácter especial (! @ # $ % & * ?)" to contrasena.any { !it.isLetterOrDigit() }
    )


    fun equipo(codigo: String): String? =
        if (codigo.isBlank()) "Selecciona el equipo probado." else null

    fun medicion(valor: String): String? {
        val limpio = valor.trim().replace(',', '.')
        if (limpio.isEmpty()) return "Ingresa el valor medido."
        val numero = limpio.toDoubleOrNull() ?: return "La medición debe ser un número (ej: 125.5)."
        return when {
            numero < 0 -> "La medición no puede ser negativa."
            numero > MAX_MEDICION -> "El valor parece fuera de rango (máx. 1.000.000)."
            else -> null
        }
    }

    fun fechaPrueba(fecha: String, hora: String, ahora: Date = Date()): String? {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply { isLenient = false }
        val instante = try {
            formato.parse("$fecha $hora")
        } catch (_: ParseException) {
            null
        } ?: return "Fecha u hora inválida."
        return if (instante.after(ahora)) "La prueba no puede tener fecha u hora futura." else null
    }

    fun observacionesPrueba(observaciones: String, rechazada: Boolean): String? = when {
        rechazada && observaciones.isBlank() -> "Describe el motivo del rechazo."
        observaciones.length > MAX_OBSERVACIONES -> "Máximo $MAX_OBSERVACIONES caracteres."
        else -> null
    }


    fun numeroProtocolo(numero: String): String? {
        val limpio = numero.trim()
        return when {
            limpio.isEmpty() -> "Ingresa el número del protocolo."
            !NUMERO_PROTOCOLO.matches(limpio) -> "Usa 6 dígitos, por ejemplo #000130."
            else -> null
        }
    }

    fun responsable(nombre: String): String? {
        val limpio = nombre.trim()
        return when {
            limpio.isEmpty() -> "Ingresa el responsable de la inspección."
            limpio.length < 3 -> "El nombre debe tener al menos 3 letras."
            !SOLO_LETRAS.matches(limpio) -> "El nombre solo puede contener letras."
            else -> null
        }
    }

    fun observaciones(texto: String): String? =
        if (texto.length > MAX_OBSERVACIONES) "Máximo $MAX_OBSERVACIONES caracteres." else null
}
