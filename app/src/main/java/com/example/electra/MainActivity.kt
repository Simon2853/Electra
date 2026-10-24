package com.example.electra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.electra.navigation.RootNavigation
import com.example.electra.ui.theme.ElectraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ElectraTheme {
                RootNavigation((application as ElectraApplication).contenedor)
            }
        }
    }
}