package com.example.electra

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.navigation.RootNavigation
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

            ElectraTheme(oscuro = oscuro) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Fondo)
                        .safeDrawingPadding()
                ) {
                    RootNavigation(contenedor)
                }
            }
        }
    }
}
