package com.example.electra.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ÚNICO lugar donde se configura la dirección del backend (el API Gateway, puerto 8080).
 *
 * - Emulador con el backend en este mismo computador: "http://10.0.2.2:8080/"
 *   (10.0.2.2 es el "localhost" del computador visto desde el emulador).
 * - Teléfono físico: la URL pública del reenvío de puertos de VS Code,
 *   p. ej. "https://abc123-8080.brs.devtunnels.ms/". Cambia cada vez que se crea el túnel.
 *
 * Debe terminar en "/".
 */
object ConfiguracionApi {

    const val BASE_URL = "http://10.0.2.2:8080/"

    /**
     * true si [BASE_URL] es una dirección de la red local (10.x, 192.168.x, 172.16-31.x). Desde Android 17
     * eso exige el permiso ACCESS_LOCAL_NETWORK, que MainActivity pide al abrir la app.
     */
    val usaRedLocal: Boolean
        get() {
            val host = BASE_URL.substringAfter("://").substringBefore('/').substringBefore(':')
            val partes = host.split('.').mapNotNull { it.toIntOrNull() }
            if (partes.size != 4) return false
            return partes[0] == 10 ||
                (partes[0] == 192 && partes[1] == 168) ||
                (partes[0] == 172 && partes[1] in 16..31)
        }

    /**
     * Las fotos se guardan en el servidor con ruta relativa ("/api/archivos/abc"); aquí se les antepone
     * la URL base para que Coil pueda descargarlas. Así siguen sirviendo aunque cambie la URL pública.
     */
    fun urlCompleta(ruta: String): String =
        if (ruta.startsWith("http://") || ruta.startsWith("https://")) ruta
        else BASE_URL.trimEnd('/') + "/" + ruta.trimStart('/')

    /** Lo contrario de [urlCompleta]: de una URL de este servidor deja solo la ruta que se guarda. */
    fun rutaRelativa(url: String): String = url.removePrefix(BASE_URL.trimEnd('/'))

    /** Cliente HTTP compartido por Retrofit (datos) y Coil (imágenes). */
    val clienteHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS) // subir una foto puede tardar más
            .addInterceptor { cadena ->
                // Dev Tunnels de VS Code muestra una página de advertencia a los navegadores; esto la omite.
                cadena.proceed(cadena.request().newBuilder().header("X-Tunnel-Skip-AntiPhishing-Page", "true").build())
            }
            .build()
    }

    /** Retrofit convierte las interfaces de [Apis] en llamadas HTTP y el JSON en data classes (Gson). */
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
