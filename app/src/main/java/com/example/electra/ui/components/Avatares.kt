package com.example.electra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.electra.R
import coil.compose.AsyncImage
import com.example.electra.model.Avatar
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.Dimens
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.TextoSecundario

data class ColorPaleta(val clave: String, val nombre: String, val color: Color)

// Para sumar un color: agregar una línea en "paleta" con una clave nueva (no cambiar las existentes).
object Avatares {
    val paleta: List<ColorPaleta> = listOf(
        ColorPaleta("blanco", "Blanco", Color(0xFFFFFFFF)),
        ColorPaleta("hielo", "Hielo", Color(0xFFEAF3FA)),
        ColorPaleta("menta", "Menta", Color(0xFFE0F2F1)),
        ColorPaleta("crema", "Crema", Color(0xFFFFF8E1)),
        ColorPaleta("rosado", "Rosado", Color(0xFFFCE4EC)),
        ColorPaleta("lavanda", "Lavanda", Color(0xFFEDE7F6)),
        ColorPaleta("celeste", "Celeste", Color(0xFF4FC3F7)),
        ColorPaleta("azul", "Azul", Color(0xFF1769AA)),
        ColorPaleta("marino", "Marino", Color(0xFF173B57)),
        ColorPaleta("indigo", "Índigo", Color(0xFF3949AB)),
        ColorPaleta("morado", "Morado", Color(0xFF7B1FA2)),
        ColorPaleta("fucsia", "Fucsia", Color(0xFFC2185B)),
        ColorPaleta("rojo", "Rojo", Color(0xFFC62828)),
        ColorPaleta("coral", "Coral", Color(0xFFFF7043)),
        ColorPaleta("naranja", "Naranja", Color(0xFFE68A00)),
        ColorPaleta("amarillo", "Amarillo", Color(0xFFFBC02D)),
        ColorPaleta("lima", "Lima", Color(0xFF9CCC65)),
        ColorPaleta("verde", "Verde", Color(0xFF2E7D32)),
        ColorPaleta("turquesa", "Turquesa", Color(0xFF00897B)),
        ColorPaleta("cafe", "Café", Color(0xFF6D4C41)),
        ColorPaleta("gris", "Gris", Color(0xFF78909C)),
        ColorPaleta("grafito", "Grafito", Color(0xFF37474F)),
        ColorPaleta("negro", "Negro", Color(0xFF111111))
    )

    // Paleta corta para el círculo de iniciales: tonos de la marca, todos con buen contraste para texto blanco.
    val paletaMarca: List<ColorPaleta> = listOf("azul", "marino", "indigo", "turquesa", "verde", "naranja", "rojo", "grafito")
        .map { clave -> paleta.first { it.clave == clave } }

    fun colorPaleta(clave: String, porDefecto: String = Avatar.COLOR_POR_DEFECTO): ColorPaleta =
        paleta.firstOrNull { it.clave == clave } ?: paleta.first { it.clave == porDefecto }

    fun color(clave: String): Color = colorPaleta(clave).color

    fun fondo(clave: String): Color = colorPaleta(clave, Avatar.FONDO_POR_DEFECTO).color

    // Texto o ícono que se lee bien encima de "fondo".
    fun contraste(fondo: Color): Color = if (fondo.luminance() > 0.5f) AzulOscuro else Color.White

    fun contrasteSecundario(fondo: Color): Color =
        if (fondo.luminance() > 0.5f) TextoSecundario else Color.White.copy(alpha = 0.8f)
}

/**
 * Avatar del técnico: su foto de perfil si tiene una; si no (o si la foto ya no se puede leer),
 * un círculo del color elegido con sus iniciales.
 */
@Composable
fun AvatarUsuario(
    avatar: Avatar,
    nombre: String,
    modifier: Modifier = Modifier,
    tamano: Dp = Dimens.tamanoAvatar,
    borde: Dp = Dimens.bordeAvatar
) {
    val fondo = Avatares.color(avatar.color)
    var fotoFallida by remember(avatar.foto) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(tamano)
            .shadow(elevation = 4.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(fondo)
            .border(borde, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (avatar.foto != null && !fotoFallida) {
            AsyncImage(
                model = avatar.foto,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                onError = { fotoFallida = true },
                modifier = Modifier.matchParentSize()
            )
        } else {
            val tamanoTexto = with(LocalDensity.current) { (tamano * 0.38f).toSp() }
            Text(
                text = Avatar.iniciales(nombre),
                color = Avatares.contraste(fondo),
                fontSize = tamanoTexto,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.semantics { contentDescription = "Avatar con iniciales" }
            )
        }
    }
}

/** Fila con la paleta corta de la marca para el círculo de iniciales. */
@Composable
fun SelectorColorAvatar(
    seleccionado: String,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Avatares.paletaMarca.forEach { opcion ->
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onSeleccionar(opcion.clave) }
                    .semantics { contentDescription = "Color ${opcion.nombre}" }
            ) {
                MuestraColor(opcion.color, activo = opcion.clave == seleccionado, tamano = 40.dp)
            }
        }
    }
}

/** Fila que muestra el color elegido; al tocarla abre la paleta completa. */
@Composable
fun SelectorColor(
    titulo: String,
    seleccionado: ColorPaleta,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var abierta by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFDDE3EA), RoundedCornerShape(12.dp))
            .clickable { abierta = true }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MuestraColor(seleccionado.color, activo = false, tamano = 32.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontSize = 12.sp, color = TextoSecundario)
            Text(seleccionado.nombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AzulOscuro)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = "Ver colores", tint = TextoSecundario)
    }

    if (abierta) {
        DialogoPaleta(
            titulo = titulo,
            seleccionado = seleccionado.clave,
            onSeleccionar = {
                onSeleccionar(it)
                abierta = false
            },
            onCerrar = { abierta = false }
        )
    }
}

@Composable
private fun DialogoPaleta(
    titulo: String,
    seleccionado: String,
    onSeleccionar: (String) -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(titulo, fontWeight = FontWeight.Bold, color = AzulOscuro) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Avatares.paleta.chunked(4).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fila.forEach { opcion ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(48.dp)
                                    .clickable { onSeleccionar(opcion.clave) }
                            ) {
                                MuestraColor(opcion.color, activo = opcion.clave == seleccionado, tamano = 38.dp)
                                Text(opcion.nombre, fontSize = 10.sp, color = TextoSecundario, maxLines = 1)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) { Text("Cerrar", color = TextoSecundario) }
        }
    )
}

@Composable
private fun MuestraColor(color: Color, activo: Boolean, tamano: Dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (activo) 3.dp else 1.dp,
                color = if (activo) AzulOscuro else Color(0xFFDDE3EA),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (activo) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = "Seleccionado",
                tint = Avatares.contraste(color),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarUsuarioPreview() {
    ElectraTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AvatarUsuario(avatar = Avatar(color = "verde"), nombre = "Ana Torres")
            SelectorColorAvatar(seleccionado = "verde", onSeleccionar = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectorColorPreview() {
    ElectraTheme {
        SelectorColor(
            titulo = "Color del avatar",
            seleccionado = Avatares.colorPaleta("morado"),
            onSeleccionar = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
