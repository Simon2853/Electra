package com.example.electra.data.remote

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Response
import java.io.IOException

/**
 * Estado de la comunicación con el servidor, para mostrar un aviso en toda la app.
 *
 * Los repositorios remotos hacen cada llamada con [llamar]: si no hay red o el servidor no responde,
 * devuelve null (en vez de lanzar una excepción que cerraría la app) y marca [sinConexion].
 * Apenas una llamada vuelve a responder, el aviso desaparece solo.
 */
class Conexion {

    private val _sinConexion = MutableStateFlow(false)
    val sinConexion: StateFlow<Boolean> = _sinConexion.asStateFlow()

    suspend fun <T> llamar(llamada: suspend () -> Response<T>): Response<T>? =
        try {
            llamada().also {
                // 502/503/504: el Gateway respondió, pero el microservicio de atrás está caído.
                _sinConexion.value = it.code() in 502..504
            }
        } catch (e: IOException) {
            // Queda en Logcat (filtro "Electra") para saber por qué falló: tiempo agotado, URL mal escrita...
            runCatching { Log.w("Electra", "Sin respuesta del servidor: ${e.javaClass.simpleName}: ${e.message}") }
            _sinConexion.value = true
            null
        }
}

/** true si el servidor respondió con el código esperado (p. ej. 201 al crear). */
fun Response<*>?.respondio(codigo: Int): Boolean = this?.code() == codigo

/** El cuerpo solo si la respuesta fue exitosa (2xx). */
fun <T> Response<T>?.cuerpo(): T? = this?.takeIf { it.isSuccessful }?.body()
