package com.example.electra.device

import android.content.Context
import android.content.Intent

object Compartir {

    fun texto(context: Context, asunto: String, cuerpo: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, cuerpo)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir respaldo"))
    }
}
