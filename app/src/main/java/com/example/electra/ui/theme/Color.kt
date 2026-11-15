package com.example.electra.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colores de la marca para un modo (claro u oscuro). Los nombres son los de siempre para no tocar
 * las pantallas, pero describen un ROL, no un tono fijo: por ejemplo [azulOscuro] es el color de
 * títulos y textos principales, que en modo oscuro es claro.
 */
data class PaletaElectra(
    /** Títulos y textos principales. */
    val azulOscuro: Color,
    /** Color de acción (botones, enlaces, íconos activos). */
    val azulPrincipal: Color,
    /** Fondo suave de chips y destacados azules. */
    val azulSuave: Color,
    /** Fondo de pantalla. */
    val fondo: Color,
    /** Fondo de tarjetas, barras y campos de texto. */
    val superficie: Color,
    /** Bordes y divisores. */
    val borde: Color,
    /** Texto e íconos sobre [azulPrincipal]. */
    val sobrePrimario: Color,
    val verdeExito: Color,
    val verdeSuave: Color,
    val rojoError: Color,
    val rojoSuave: Color,
    val naranjaPendiente: Color,
    val naranjaSuave: Color,
    val textoSecundario: Color
)

val PaletaClara = PaletaElectra(
    azulOscuro = Color(0xFF173B57),
    azulPrincipal = Color(0xFF1769AA),
    azulSuave = Color(0xFFEAF3FA),
    fondo = Color(0xFFF4F7FA),
    superficie = Color.White,
    borde = Color(0xFFDDE3EA),
    sobrePrimario = Color.White,
    verdeExito = Color(0xFF2E7D32),
    verdeSuave = Color(0xFFE8F5E9),
    rojoError = Color(0xFFC62828),
    rojoSuave = Color(0xFFFDECEA),
    naranjaPendiente = Color(0xFFE68A00),
    naranjaSuave = Color(0xFFFFF3E0),
    textoSecundario = Color(0xFF6B7280)
)

/** Misma marca sobre fondo azul noche; los tonos de acento se aclaran para mantener el contraste. */
val PaletaOscura = PaletaElectra(
    azulOscuro = Color(0xFFE3ECF4),
    azulPrincipal = Color(0xFF6CB4EE),
    azulSuave = Color(0xFF1C3347),
    fondo = Color(0xFF0E1620),
    superficie = Color(0xFF18222E),
    borde = Color(0xFF2C3A48),
    sobrePrimario = Color(0xFF0B1A28),
    verdeExito = Color(0xFF81C784),
    verdeSuave = Color(0xFF1C3322),
    rojoError = Color(0xFFEF7070),
    rojoSuave = Color(0xFF3D2020),
    naranjaPendiente = Color(0xFFFFB74D),
    naranjaSuave = Color(0xFF3B2B14),
    textoSecundario = Color(0xFF9AA6B2)
)

/** Paleta activa; la fija [ElectraTheme]. */
val LocalPaleta = staticCompositionLocalOf { PaletaClara }

// Accesos directos a la paleta activa: se usan igual que antes (color = AzulOscuro), pero
// cambian con el tema. Solo pueden leerse dentro de funciones @Composable.
val AzulOscuro: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.azulOscuro
val AzulPrincipal: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.azulPrincipal
val AzulSuave: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.azulSuave
val Fondo: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.fondo
val Superficie: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.superficie
val Borde: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.borde
val SobrePrimario: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.sobrePrimario
val VerdeExito: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.verdeExito
val VerdeSuave: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.verdeSuave
val RojoError: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.rojoError
val RojoSuave: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.rojoSuave
val NaranjaPendiente: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.naranjaPendiente
val NaranjaSuave: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.naranjaSuave
val TextoSecundario: Color @Composable @ReadOnlyComposable get() = LocalPaleta.current.textoSecundario

/** Blanco fijo: solo para texto sobre fotos o imágenes, que no cambian con el tema. */
val Blanco = Color.White
