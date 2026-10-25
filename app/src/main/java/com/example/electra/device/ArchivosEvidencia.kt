package com.example.electra.device

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File


object ArchivosEvidencia {

    private const val CARPETA = "evidencias"

    fun nuevaFoto(context: Context): Uri {
        val carpeta = File(context.filesDir, CARPETA).apply { mkdirs() }
        val archivo = File(carpeta, "prueba_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    fun eliminar(context: Context, uri: String) {
        runCatching { context.contentResolver.delete(uri.toUri(), null, null) }
    }
}
