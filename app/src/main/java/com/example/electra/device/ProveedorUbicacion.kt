package com.example.electra.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import android.os.CancellationSignal
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class Coordenadas(val latitud: Double, val longitud: Double)

sealed interface ResultadoUbicacion {
    data class Exito(val coordenadas: Coordenadas) : ResultadoUbicacion
    data object SinPermiso : ResultadoUbicacion
    data object GpsDesactivado : ResultadoUbicacion
    data object NoDisponible : ResultadoUbicacion
}

fun interface ProveedorUbicacion {
    suspend fun obtener(): ResultadoUbicacion
}

class ProveedorUbicacionAndroid(context: Context) : ProveedorUbicacion {

    private val contexto = context.applicationContext
    private val manager = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override suspend fun obtener(): ResultadoUbicacion {
        if (!tienePermiso()) return ResultadoUbicacion.SinPermiso
        if (!LocationManagerCompat.isLocationEnabled(manager)) return ResultadoUbicacion.GpsDesactivado

        val proveedor = when {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return ResultadoUbicacion.GpsDesactivado
        }

        val ubicacion = withTimeoutOrNull(TIEMPO_MAXIMO_MS) { ubicacionActual(proveedor) }
            ?: ultimaConocida()

        return ubicacion
            ?.let { ResultadoUbicacion.Exito(Coordenadas(it.latitude, it.longitude)) }
            ?: ResultadoUbicacion.NoDisponible
    }

    @Suppress("MissingPermission")
    private suspend fun ubicacionActual(proveedor: String): Location? =
        suspendCancellableCoroutine { continuacion ->
            val cancelar = CancellationSignal()
            continuacion.invokeOnCancellation { cancelar.cancel() }
            LocationManagerCompat.getCurrentLocation(
                manager,
                proveedor,
                cancelar,
                ContextCompat.getMainExecutor(contexto)
            ) { ubicacion -> if (continuacion.isActive) continuacion.resume(ubicacion) }
        }

    @Suppress("MissingPermission")
    private fun ultimaConocida(): Location? =
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }

    private fun tienePermiso(): Boolean =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED }

    private companion object {
        const val TIEMPO_MAXIMO_MS = 15_000L
    }
}
