package com.example.electra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electra.data.UsuariosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroUiState(
    val error: String = "",
    val registrando: Boolean = false,
    val registroExitoso: Boolean = false
)

class RegistroViewModel(
    private val usuarios: UsuariosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun registrar(nombre: String, usuario: String, contrasena: String, confirmar: String) {
        if (_uiState.value.registrando) return

        val error = validarRegistro(nombre, usuario, contrasena, confirmar)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        _uiState.update { it.copy(registrando = true) }
        viewModelScope.launch {
            val ok = usuarios.registrar(nombre.trim(), usuario.trim(), contrasena)
            _uiState.update {
                it.copy(
                    registrando = false,
                    error = if (ok) "" else "Ese usuario ya existe.",
                    registroExitoso = ok
                )
            }
        }
    }

    fun registroConsumido() {
        _uiState.update { it.copy(registroExitoso = false) }
    }
}

/** Devuelve el mensaje de error del formulario, o null si es válido. */
fun validarRegistro(
    nombre: String,
    usuario: String,
    contrasena: String,
    confirmar: String
): String? = when {
    nombre.isBlank() || usuario.isBlank() || contrasena.isBlank() || confirmar.isBlank() ->
        "Completa todos los campos."

    usuario.trim().contains(" ") ->
        "El usuario no debe contener espacios."

    else -> validarContrasena(nombre, usuario, contrasena)
        ?: if (contrasena != confirmar) "Las contraseñas no coinciden." else null
}

fun validarContrasena(
    nombre: String,
    usuario: String,
    contrasena: String
): String? {
    if (contrasena.length < 8) {
        return "La clave debe tener al menos 8 caracteres."
    }

    if (contrasena.length > 20) {
        return "La clave no puede superar los 20 caracteres."
    }

    if (contrasena.none { it.isUpperCase() }) {
        return "La clave debe incluir al menos una letra mayúscula."
    }

    if (contrasena.none { it.isLowerCase() }) {
        return "La clave debe incluir al menos una letra minúscula."
    }

    if (contrasena.none { it.isDigit() }) {
        return "La clave debe incluir al menos un número."
    }

    if (contrasena.none { !it.isLetterOrDigit() }) {
        return "La clave debe incluir un carácter especial: ! @ # $ % & * ?"
    }

    val normalizada = contrasena
        .lowercase()
        .filter { it.isLetterOrDigit() }

    if ("electra" in normalizada) {
        return "No incluyas el nombre de la aplicación."
    }

    val datosPersonales = (nombre + " " + usuario)
        .lowercase()
        .split(Regex("[^\\p{L}\\p{N}]+"))
        .filter { it.length >= 3 }

    if (datosPersonales.any { it in normalizada }) {
        return "No incluyas tu nombre ni tu usuario."
    }

    return null
}
