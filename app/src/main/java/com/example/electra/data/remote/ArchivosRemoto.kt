package com.example.electra.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Resultado de dejar una foto lista para guardarla en otro servicio. */
sealed interface FotoPreparada {
    /** No hay foto. */
    data object SinFoto : FotoPreparada

    /** [ruta] es la del servidor; [subidaDesde] es la Uri local que se subió (para borrarla), o null si ya estaba arriba. */
    data class Lista(val ruta: String, val subidaDesde: String?) : FotoPreparada

    /** No se pudo subir (sin conexión o el servidor la rechazó). */
    data object Fallo : FotoPreparada
}

/**
 * Sube y elimina fotos en archivos-service. Las fotos se toman con la cámara y quedan en el teléfono
 * (Uri content://); al guardar una prueba o el perfil se suben y se guarda solo la ruta que devuelve el servidor.
 *
 * @param leerLocal lee los bytes de una Uri local (lo resuelve AppContainer con el ContentResolver).
 */
class ArchivosRemoto(
    private val api: ArchivosApi,
    private val conexion: Conexion,
    private val leerLocal: (String) -> ByteArray?
) {

    fun esLocal(uri: String): Boolean = uri.startsWith("content://") || uri.startsWith("file://")

    suspend fun preparar(foto: String?, categoria: String, usuario: String): FotoPreparada {
        if (foto.isNullOrBlank()) return FotoPreparada.SinFoto
        if (!esLocal(foto)) return FotoPreparada.Lista(ConfiguracionApi.rutaRelativa(foto), subidaDesde = null)

        val bytes = leerLocal(foto) ?: return FotoPreparada.Fallo
        val parte = MultipartBody.Part.createFormData(
            "archivo", "foto.jpg", bytes.toRequestBody("image/jpeg".toMediaType())
        )
        val ruta = conexion.llamar { api.subir(parte, categoria, usuario) }.cuerpo()?.ruta
        return if (ruta != null) FotoPreparada.Lista(ruta, subidaDesde = foto) else FotoPreparada.Fallo
    }

    /** Borra del servidor una foto por su URL o ruta ("/api/archivos/{id}"). */
    suspend fun eliminar(urlORuta: String, usuario: String): Boolean {
        val id = urlORuta.substringAfterLast('/').takeIf { it.isNotBlank() } ?: return false
        return conexion.llamar { api.eliminar(id, usuario) }.respondio(204)
    }
}
