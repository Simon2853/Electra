package com.example.electra.ui.screen

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingFlat
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.electra.device.Compartir
import com.example.electra.model.OpcionesPrueba
import com.example.electra.model.Respaldo
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.RojoError
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.theme.VerdeExito
import com.example.electra.ui.viewmodel.Comparacion
import com.example.electra.ui.viewmodel.DetallePruebaViewModel
import java.util.Locale

@Composable
fun DetallePrueba(
    idPrueba: Long,
    viewModel: DetallePruebaViewModel,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val prueba = estado.prueba

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(
            titulo = "Detalle de la prueba",
            subtitulo = prueba?.equipo ?: "Prueba #$idPrueba",
            onVolver = onBackClick,
            acciones = {
                if (prueba != null) {
                    IconButton(onClick = {
                        viewModel.textoRespaldo()?.let { Compartir.texto(context, "Prueba ${prueba.equipo}", it) }
                    }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Compartir con el cliente", tint = AzulPrincipal)
                    }
                }
            }
        )

        if (prueba == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (estado.cargando) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = "No se encontró la prueba solicitada (ID: $idPrueba).",
                        color = TextoSecundario,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
            return@Column
        }

        val aprobado = prueba.resultado == OpcionesPrueba.APROBADO
        val colorEstado = if (aprobado) VerdeExito else RojoError


        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + scaleIn(spring(Spring.DampingRatioMediumBouncy), initialScale = 0.9f)
            ) {
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (aprobado) Icons.Filled.CheckCircle else Icons.Filled.Error,
                            contentDescription = null,
                            tint = colorEstado,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(prueba.equipo, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                            Text(
                                "Estado: ${prueba.resultado.uppercase()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorEstado
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { it / 4 }) {
                Tarjeta {
                    Text("Información general", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    FilaDato(Icons.AutoMirrored.Outlined.Assignment, "Tipo de prueba", prueba.tipoPrueba)
                    FilaDato(Icons.Outlined.CalendarToday, "Fecha y hora", "${prueba.fecha} a las ${prueba.hora}")
                    FilaDato(Icons.Outlined.Speed, "Medición registrada", "${prueba.medicion} ${prueba.unidad}", destacado = true)
                    estado.comparacion?.let { TarjetaComparacion(it, prueba.unidad) }
                }
            }

            AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { it / 3 }) {
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Outlined.Comment, contentDescription = null, tint = AzulPrincipal, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Observaciones", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    }
                    Text(
                        text = prueba.observaciones.ifBlank { "Sin observaciones registradas." },
                        fontSize = 14.sp,
                        color = if (prueba.observaciones.isNotBlank()) AzulOscuro else TextoSecundario
                    )
                }
            }

            if (prueba.fotoUri != null || prueba.tieneUbicacion) {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { it / 2 }) {
                    Tarjeta {
                        Text("Evidencia en terreno", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                        prueba.fotoUri?.let { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "Foto de evidencia del equipo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                        Respaldo.coordenadas(prueba)?.let { coordenadas ->
                            FilaDato(Icons.Outlined.LocationOn, "Ubicación GPS", coordenadas)
                            OutlinedButton(
                                onClick = {

                                    val geo = "geo:0,0?q=$coordenadas(${prueba.equipo})".toUri()
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, geo)) }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.Map, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Ver en el mapa")
                            }
                        }
                    }
                }
            }

            Text(
                text = "ELECTRA • Induztek Ingeniería SpA",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = TextoSecundario
            )
        }
    }
}


@Composable
private fun TarjetaComparacion(comparacion: Comparacion, unidad: String) {
    val icono = when {
        comparacion.diferencia > 0 -> Icons.AutoMirrored.Outlined.TrendingUp
        comparacion.diferencia < 0 -> Icons.AutoMirrored.Outlined.TrendingDown
        else -> Icons.AutoMirrored.Outlined.TrendingFlat
    }
    val porcentaje = comparacion.porcentaje?.let { String.format(Locale.US, " (%+.1f%%)", it) }.orEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Fondo, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = AzulPrincipal)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = String.format(Locale.US, "%+.2f %s", comparacion.diferencia, unidad) + porcentaje,
                fontWeight = FontWeight.Bold,
                color = AzulOscuro,
                fontSize = 14.sp
            )
            Text(
                text = "vs. ${comparacion.anterior.medicion} $unidad del ${comparacion.anterior.fecha}",
                fontSize = 12.sp,
                color = TextoSecundario
            )
        }
    }
}

@Composable
private fun Tarjeta(contenido: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            contenido()
        }
    }
}

@Composable
private fun FilaDato(icono: ImageVector, etiqueta: String, valor: String, destacado: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = AzulPrincipal, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(etiqueta, fontSize = 12.sp, color = TextoSecundario)
            Text(
                valor,
                fontSize = if (destacado) 22.sp else 15.sp,
                fontWeight = if (destacado) FontWeight.Bold else FontWeight.SemiBold,
                color = AzulOscuro
            )
        }
    }
}
