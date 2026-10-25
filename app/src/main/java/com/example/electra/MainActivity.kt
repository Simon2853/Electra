package com.example.electra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import com.example.electra.navigation.RootNavigation
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ElectraTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Fondo)
                        .safeDrawingPadding()
                ) {
                    RootNavigation((application as ElectraApplication).contenedor)
                }
            }
        }
    }
}
