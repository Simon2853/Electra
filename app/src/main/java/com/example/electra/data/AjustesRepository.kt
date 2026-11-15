package com.example.electra.data

import android.content.Context
import androidx.core.content.edit
import com.example.electra.model.ModoTema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Ajustes del dispositivo (no de una cuenta): se aplican también en el login.
 * [tema] es un flujo para que el cambio se vea al instante en toda la app.
 */
interface AjustesRepository {
    val tema: StateFlow<ModoTema>
    fun cambiarTema(modo: ModoTema)
}

class AjustesPreferencias(context: Context) : AjustesRepository {

    private val prefs = context.getSharedPreferences("electra_ajustes", Context.MODE_PRIVATE)

    private val _tema = MutableStateFlow(ModoTema.desdeClave(prefs.getString(CLAVE_TEMA, null)))
    override val tema: StateFlow<ModoTema> = _tema.asStateFlow()

    override fun cambiarTema(modo: ModoTema) {
        prefs.edit { putString(CLAVE_TEMA, modo.clave) }
        _tema.value = modo
    }

    companion object {
        private const val CLAVE_TEMA = "tema"
    }
}

class AjustesEnMemoria(inicial: ModoTema = ModoTema.POR_DEFECTO) : AjustesRepository {
    private val _tema = MutableStateFlow(inicial)
    override val tema: StateFlow<ModoTema> = _tema.asStateFlow()

    override fun cambiarTema(modo: ModoTema) {
        _tema.value = modo
    }
}
