package com.example.electra.data.remote

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

    /**
     * Cuántas veces volvió la conexión después de perderse. Las pantallas lo observan para recargar
     * sus datos al reconectar (lo que mostraban sin conexión estaba vacío o desactualizado).
     */
    private val _reconexiones = MutableStateFlow(0)
    val reconexiones: StateFlow<Int> = _reconexiones.asStateFlow()

    suspend fun <T> llamar(llamada: suspend () -> Response<T>): Response<T>? =
        try {
            llamada().also {
                // 502/503/504: el Gateway respondió, pero el microservicio de atrás está caído.
                cambiarEstado(caido = it.code() in 502..504)
            }
        } catch (e: IOException) {
            // Queda en Logcat (filtro "Electra") para saber por qué falló: tiempo agotado, URL mal escrita...
            runCatching { Log.w("Electra", "Sin respuesta del servidor: ${e.javaClass.simpleName}: ${e.message}") }
            cambiarEstado(caido = true)
            null
        }

    private fun cambiarEstado(caido: Boolean) {
        if (_sinConexion.value && !caido) _reconexiones.value++
        _sinConexion.value = caido
    }

    /**
     * Reintento automático: mientras [sinConexion] sea true, ejecuta [ping] cada [intervaloMs].
     * [ping] debe llamar al servidor con [llamar]; cuando responda, el aviso se apaga y se dejan de
     * hacer reintentos (con conexión no hay llamadas extra). Si se vuelve a caer, empieza de nuevo.
     */
    fun vigilar(alcance: CoroutineScope, intervaloMs: Long = 10_000, ping: suspend () -> Unit): Job =
        alcance.launch {
            // collectLatest cancela el ciclo de reintentos apenas el estado cambia a "con conexión".
            sinConexion.collectLatest { caido ->
                while (caido) {
                    delay(intervaloMs)
                    ping()
                }
            }
        }
}

/** true si el servidor respondió con el código esperado (p. ej. 201 al crear). */
fun Response<*>?.respondio(codigo: Int): Boolean = this?.code() == codigo

/** El cuerpo solo si la respuesta fue exitosa (2xx). */
fun <T> Response<T>?.cuerpo(): T? = this?.takeIf { it.isSuccessful }?.body()
