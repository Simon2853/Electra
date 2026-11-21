package com.example.electra

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.data.remote.ConfiguracionApi
import com.example.electra.navigation.RootNavigation
import com.example.electra.ui.components.AvisoSinConexion
import com.example.electra.ui.components.LocalReconexiones
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo

class MainActivity : ComponentActivity() {

    // Si el usuario lo rechaza, las llamadas fallan y se ve el aviso "Sin conexión con el servidor".
    private val pedirRedLocal = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        // Muestra el Splash (Theme.Electra.Splash) mientras la app arranca; debe ir antes de super.onCreate.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pedirPermisoRedLocal()

        val contenedor = (application as ElectraApplication).contenedor

        setContent {
            val modoTema by contenedor.ajustes.tema.collectAsStateWithLifecycle()
            val oscuro = modoTema.esOscuro(isSystemInDarkTheme())

            // Íconos de la barra de estado y de navegación según el tema elegido, no solo el del sistema.
            DisposableEffect(oscuro) {
                val estilo = if (oscuro) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = estilo, navigationBarStyle = estilo)
                onDispose {}
            }

            val sinConexion by contenedor.conexion.sinConexion.collectAsStateWithLifecycle()
            val reconexiones by contenedor.conexion.reconexiones.collectAsStateWithLifecycle()

            ElectraTheme(oscuro = oscuro) {
                // Cada pantalla recarga sus datos cuando cambia este número (ver AlReconectar).
                CompositionLocalProvider(LocalReconexiones provides reconexiones) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Fondo)
                            .safeDrawingPadding()
                    ) {
                        AvisoSinConexion(visible = sinConexion)
                        Box(modifier = Modifier.weight(1f)) {
                            RootNavigation(contenedor)
                        }
                    }
                }
            }
        }
    }

    /**
     * Android 17 bloquea las conexiones a la red local sin el permiso ACCESS_LOCAL_NETWORK. Solo se pide
     * cuando el backend está en la red local (emulador o mismo Wi-Fi); con la URL pública del túnel no hace falta.
     */
    private fun pedirPermisoRedLocal() {
        if (Build.VERSION.SDK_INT < 37 || !ConfiguracionApi.usaRedLocal) return
        if (checkSelfPermission(PERMISO_RED_LOCAL) == PackageManager.PERMISSION_GRANTED) return
        pedirRedLocal.launch(PERMISO_RED_LOCAL)
    }

    private companion object {
        const val PERMISO_RED_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK"
    }
}
