package com.example.electra.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.device.Compartir
import com.example.electra.model.OpcionesPrueba
import com.example.electra.ui.components.BotonCarga
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.RojoError
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.theme.VerdeExito
import com.example.electra.ui.theme.VerdeSuave
import com.example.electra.ui.viewmodel.DetallePruebaViewModel


@Composable
fun ConfirmacionPrueba(
    viewModel: DetallePruebaViewModel,
    onVerHistorial: (String) -> Unit = {},
    onInicio: () -> Unit = {}
) {
    val context = LocalContext.current
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val prueba = estado.prueba


    val escala = remember { Animatable(0f) }
    var mostrarContenido by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        escala.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        mostrarContenido = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(110.dp)
                .graphicsLayer {
                    scaleX = escala.value
                    scaleY = escala.value
                    rotationZ = (1f - escala.value) * -90f
                }
                .background(VerdeSuave, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Prueba registrada",
                tint = VerdeExito,
                modifier = Modifier.size(72.dp)
            )
        }

        Text("Prueba registrada", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
        Text(
            text = "Quedó guardada en el dispositivo como \"registrada en terreno\". " +
                    "Puedes compartir el respaldo con el cliente ahora mismo.",
            fontSize = 14.sp,
            color = TextoSecundario,
            textAlign = TextAlign.Center
        )

        AnimatedVisibility(
            visible = mostrarContenido && prueba != null,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
        ) {
            if (prueba == null) return@AnimatedVisibility
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Resumen", fontWeight = FontWeight.Bold, color = AzulOscuro, fontSize = 16.sp)
                        HorizontalDivider()
                        FilaResumen("Equipo", prueba.equipo)
                        FilaResumen("Prueba", prueba.tipoPrueba)
                        FilaResumen("Fecha", "${prueba.fecha} ${prueba.hora}")
                        FilaResumen("Medición", "${prueba.medicion} ${prueba.unidad}")
                        FilaResumen(
                            "Resultado",
                            prueba.resultado,
                            color = if (prueba.resultado == OpcionesPrueba.APROBADO) VerdeExito else RojoError
                        )
                        if (prueba.fotoUri != null) FilaResumen("Foto", "Adjunta")
                        if (prueba.tieneUbicacion) FilaResumen("GPS", "Registrado")
                    }
                }

                BotonCarga(
                    texto = "Compartir respaldo",
                    icono = Icons.Outlined.Share,
                    cargando = false,
                    onClick = {
                        viewModel.textoRespaldo()?.let { Compartir.texto(context, "Prueba ${prueba.equipo}", it) }
                    },
                    color = VerdeExito
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { onVerHistorial(prueba.equipo) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.History, contentDescription = null, tint = AzulPrincipal)
                        Spacer(Modifier.width(6.dp))
                        Text("Historial", color = AzulPrincipal)
                    }
                    OutlinedButton(
                        onClick = onInicio,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Home, contentDescription = null, tint = AzulPrincipal)
                        Spacer(Modifier.width(6.dp))
                        Text("Inicio", color = AzulPrincipal)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String, color: Color = AzulOscuro) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(etiqueta, fontSize = 14.sp, color = TextoSecundario, modifier = Modifier.weight(1f))
        Text(valor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}
