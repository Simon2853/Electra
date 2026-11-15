package com.example.electra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun esquemaClaro(p: PaletaElectra): ColorScheme = lightColorScheme(
    primary = p.azulPrincipal,
    onPrimary = p.sobrePrimario,
    primaryContainer = p.azulSuave,
    onPrimaryContainer = p.azulOscuro,
    secondary = p.azulOscuro,
    onSecondary = p.superficie,
    tertiary = p.verdeExito,
    onTertiary = p.superficie,
    error = p.rojoError,
    onError = p.superficie,
    errorContainer = p.rojoSuave,
    background = p.fondo,
    onBackground = p.azulOscuro,
    surface = p.superficie,
    onSurface = p.azulOscuro,
    onSurfaceVariant = p.textoSecundario,
    outline = p.borde,
    outlineVariant = p.borde
)

private fun esquemaOscuro(p: PaletaElectra): ColorScheme = darkColorScheme(
    primary = p.azulPrincipal,
    onPrimary = p.sobrePrimario,
    primaryContainer = p.azulSuave,
    onPrimaryContainer = p.azulOscuro,
    secondary = p.azulOscuro,
    onSecondary = p.fondo,
    tertiary = p.verdeExito,
    onTertiary = p.fondo,
    error = p.rojoError,
    onError = p.fondo,
    errorContainer = p.rojoSuave,
    background = p.fondo,
    onBackground = p.azulOscuro,
    surface = p.superficie,
    onSurface = p.azulOscuro,
    onSurfaceVariant = p.textoSecundario,
    surfaceContainerLowest = p.fondo,
    surfaceContainerLow = p.superficie,
    surfaceContainer = p.superficie,
    surfaceContainerHigh = p.superficie,
    surfaceContainerHighest = p.azulSuave,
    outline = p.borde,
    outlineVariant = p.borde
)

private val EsquemaClaro = esquemaClaro(PaletaClara)
private val EsquemaOscuro = esquemaOscuro(PaletaOscura)

/**
 * Tema de la app. [oscuro] lo decide quien llama según la preferencia guardada
 * (Sistema / Claro / Oscuro); por defecto sigue al sistema.
 */
@Composable
fun ElectraTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalPaleta provides if (oscuro) PaletaOscura else PaletaClara) {
        MaterialTheme(
            colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
            typography = Typography,
            content = content
        )
    }
}
