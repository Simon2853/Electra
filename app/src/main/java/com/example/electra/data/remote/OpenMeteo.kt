package com.example.electra.data.remote

import com.example.electra.data.ClimaRepository
import com.example.electra.model.Clima
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

// API EXTERNA: Open-Meteo (https://open-meteo.com), gratuita y sin clave.
// Va directo a Internet con su propio Retrofit: NO pasa por nuestro API Gateway ni usa Conexion,
// así una falla del clima no muestra el aviso "sin conexión con el servidor" ni afecta a los microservicios.

/** Ejemplo: GET https://api.open-meteo.com/v1/forecast?latitude=-33.45&longitude=-70.66&current=temperature_2m,... */
interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun actual(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") campos: String = "temperature_2m,relative_humidity_2m,weather_code"
    ): Response<RespuestaOpenMeteo>
}

/** Solo la parte del JSON que se usa: {"current": {"temperature_2m": 18.4, "relative_humidity_2m": 62, "weather_code": 2}} */
data class RespuestaOpenMeteo(val current: ActualOpenMeteo?)

data class ActualOpenMeteo(
    @SerializedName("temperature_2m") val temperatura: Double?,
    @SerializedName("relative_humidity_2m") val humedad: Double?,
    @SerializedName("weather_code") val codigo: Int?
)

class RemotoClimaRepository(private val api: OpenMeteoApi) : ClimaRepository {

    override suspend fun actual(latitud: Double, longitud: Double): Clima? {
        val actual = try {
            api.actual(latitud, longitud).takeIf { it.isSuccessful }?.body()?.current
        } catch (e: IOException) {
            null
        } ?: return null
        val temperatura = actual.temperatura ?: return null
        val humedad = actual.humedad ?: return null
        return Clima(temperatura, humedad.roundToInt(), actual.codigo ?: -1)
    }

    companion object {
        private const val URL_OPEN_METEO = "https://api.open-meteo.com/"

        fun crear(): RemotoClimaRepository {
            val cliente = OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(URL_OPEN_METEO)
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return RemotoClimaRepository(retrofit.create(OpenMeteoApi::class.java))
        }
    }
}
