package com.example.electra.data

import android.content.Context
import com.example.electra.device.ArchivosEvidencia
import com.example.electra.device.ProveedorUbicacion
import com.example.electra.device.ProveedorUbicacionAndroid

class AppContainer(context: Context) {

    private val contexto = context.applicationContext

    private val db by lazy { PruebaDbHelper(contexto) }

    val pruebas: PruebasRepository by lazy { SqlitePruebasRepository(db) }
    val protocolos: ProtocolosRepository by lazy { SqliteProtocolosRepository(db) }
    val usuarios: UsuariosRepository by lazy { LocalUsuariosRepository(contexto) }
    val sesion: SesionRepository by lazy { SesionPreferencias(contexto) }
    val ubicacion: ProveedorUbicacion by lazy { ProveedorUbicacionAndroid(contexto) }

    /** Borra un archivo propio (p. ej. una foto) por su Uri; así los ViewModels no dependen de Context. */
    val eliminarArchivo: (String) -> Unit = { uri -> ArchivosEvidencia.eliminar(contexto, uri) }
}
