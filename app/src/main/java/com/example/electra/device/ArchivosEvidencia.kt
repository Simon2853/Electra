package com.example.electra.device

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File


object ArchivosEvidencia {

    private const val CARPETA = "evidencias"
    private const val CARPETA_PERFIL = "perfil"

    fun nuevaFoto(context: Context): Uri = nuevoArchivo(context, CARPETA, "prueba")

    /** Archivo vacío donde la cámara deja la foto de perfil. */
    fun nuevaFotoPerfil(context: Context): Uri = nuevoArchivo(context, CARPETA_PERFIL, "perfil")

    /**
     * Copia una imagen elegida de la galería a la carpeta privada de la app.
     * El permiso sobre la Uri de la galería es temporal, por eso no se guarda la original.
     */
    fun copiarFotoPerfil(context: Context, origen: Uri): Uri? {
        val destino = nuevaFotoPerfil(context)
        val copiado = runCatching {
            context.contentResolver.openInputStream(origen)?.use { entrada ->
                context.contentResolver.openOutputStream(destino)?.use { salida -> entrada.copyTo(salida) }
            }
        }.getOrNull() != null
        if (!copiado) eliminar(context, destino.toString())
        return if (copiado) destino else null
    }

    fun eliminar(context: Context, uri: String) {
        runCatching { context.contentResolver.delete(uri.toUri(), null, null) }
    }

    // Nombre con la hora para que cada foto tenga una Uri distinta (y Coil no muestre una vieja en caché).
    private fun nuevoArchivo(context: Context, carpeta: String, prefijo: String): Uri {
        val dir = File(context.filesDir, carpeta).apply { mkdirs() }
        val archivo = File(dir, "${prefijo}_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }
}
