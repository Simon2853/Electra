package com.example.electra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaElectra = lightColorScheme(
    primary = AzulPrincipal,
    onPrimary = Color.White,
    primaryContainer = AzulSuave,
    onPrimaryContainer = AzulOscuro,
    secondary = AzulOscuro,
    onSecondary = Color.White,
    tertiary = VerdeExito,
    onTertiary = Color.White,
    error = RojoError,
    onError = Color.White,
    errorContainer = RojoSuave,
    background = Fondo,
    onBackground = AzulOscuro,
    surface = Color.White,
    onSurface = AzulOscuro,
    onSurfaceVariant = TextoSecundario
)

@Composable
fun ElectraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaElectra,
        typography = Typography,
        content = content
    )
}
