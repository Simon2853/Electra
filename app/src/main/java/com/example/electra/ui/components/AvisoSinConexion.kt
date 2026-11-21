package com.example.electra.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.electra.ui.theme.RojoError
import com.example.electra.ui.theme.RojoSuave

/** Número de reconexiones con el servidor (Conexion.reconexiones); lo entrega MainActivity a todas las pantallas. */
val LocalReconexiones = compositionLocalOf { 0 }

/**
 * Ejecuta [accion] (normalmente viewModel.cargar()) cada vez que la conexión con el servidor vuelve,
 * para que la pantalla abierta no siga mostrando lo que cargó cuando no había conexión.
 * No se ejecuta al abrir la pantalla: de eso ya se encarga ON_RESUME.
 */
@Composable
fun AlReconectar(accion: () -> Unit) {
    val reconexiones = LocalReconexiones.current
    val alAbrir = remember { reconexiones }
    LaunchedEffect(reconexiones) {
        if (reconexiones != alAbrir) accion()
    }
}

/**
 * Franja en la parte superior cuando el servidor no responde. Aparece y desaparece sola según
 * el resultado de la última llamada a los microservicios (ver Conexion).
 */
@Composable
fun AvisoSinConexion(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = visible, enter = expandVertically(), exit = shrinkVertically(), modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RojoSuave)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = RojoError, modifier = Modifier.size(18.dp))
            Text(
                text = "Sin conexión con el servidor. Los datos pueden no estar actualizados.",
                color = RojoError,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
