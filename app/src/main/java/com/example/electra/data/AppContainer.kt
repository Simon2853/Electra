package com.example.electra.data

import android.content.Context
import androidx.core.net.toUri
import com.example.electra.data.remote.ArchivosApi
import com.example.electra.data.remote.ArchivosRemoto
import com.example.electra.data.remote.Conexion
import com.example.electra.data.remote.ConfiguracionApi
import com.example.electra.data.remote.ProtocolosApi
import com.example.electra.data.remote.PruebasApi
import com.example.electra.data.remote.RemotoProtocolosRepository
import com.example.electra.data.remote.RemotoPruebasRepository
import com.example.electra.data.remote.RemotoUsuariosRepository
import com.example.electra.data.remote.UsuariosApi
import com.example.electra.device.ArchivosEvidencia
import com.example.electra.device.ProveedorUbicacion
import com.example.electra.device.ProveedorUbicacionAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Crea los repositorios que usa toda la app. Los ViewModels solo conocen las interfaces
 * (UsuariosRepository, PruebasRepository...), así que pasar de SQLite a los microservicios
 * se hizo cambiando las implementaciones aquí, sin tocar las pantallas.
 *
 * - En el servidor (Retrofit): usuarios, pruebas, protocolos y fotos.
 * - En el teléfono: la sesión abierta y el tema (son de este dispositivo, no de la cuenta).
 */
class AppContainer(context: Context) {

    private val contexto = context.applicationContext

    /** Para tareas que no dependen de una pantalla (p. ej. borrar una foto vieja del servidor). */
    private val alcanceApp = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Avisa a la interfaz cuando el servidor no responde. */
    val conexion = Conexion()

    val sesion: SesionRepository by lazy { SesionPreferencias(contexto) }
    val ajustes: AjustesRepository by lazy { AjustesPreferencias(contexto) }

    private val retrofit get() = ConfiguracionApi.retrofit

    private val archivosRemoto by lazy {
        ArchivosRemoto(retrofit.create(ArchivosApi::class.java), conexion, leerLocal = ::leerArchivoLocal)
    }

    val usuarios: UsuariosRepository by lazy {
        RemotoUsuariosRepository(
            retrofit.create(UsuariosApi::class.java), conexion, archivosRemoto, eliminarLocal = ::eliminarArchivoLocal
        )
    }

    val pruebas: PruebasRepository by lazy {
        RemotoPruebasRepository(
            retrofit.create(PruebasApi::class.java), conexion, archivosRemoto,
            usuarioActual = sesion::usuarioActual, eliminarLocal = ::eliminarArchivoLocal
        )
    }

    val protocolos: ProtocolosRepository by lazy {
        RemotoProtocolosRepository(retrofit.create(ProtocolosApi::class.java), conexion, usuarioActual = sesion::usuarioActual)
    }

    val ubicacion: ProveedorUbicacion by lazy { ProveedorUbicacionAndroid(contexto) }

    /**
     * Borra una foto que ya no se usa (p. ej. la foto de perfil anterior), esté en el teléfono o en el
     * servidor. Así los ViewModels no dependen de Context ni de Retrofit.
     */
    val eliminarArchivo: (String) -> Unit = { uri ->
        if (archivosRemoto.esLocal(uri)) {
            eliminarArchivoLocal(uri)
        } else {
            val usuario = sesion.usuarioActual()
            if (usuario != null) alcanceApp.launch { archivosRemoto.eliminar(uri, usuario) }
        }
    }

    private fun leerArchivoLocal(uri: String): ByteArray? =
        runCatching { contexto.contentResolver.openInputStream(uri.toUri())?.use { it.readBytes() } }.getOrNull()

    private fun eliminarArchivoLocal(uri: String) = ArchivosEvidencia.eliminar(contexto, uri)
}
