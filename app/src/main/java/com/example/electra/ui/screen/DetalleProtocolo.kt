package com.example.electra.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.device.Compartir
import com.example.electra.device.Vibracion
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.OpcionesPrueba
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.components.BotonCarga
import com.example.electra.ui.components.CampoValidado
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.NaranjaPendiente
import com.example.electra.ui.theme.NaranjaSuave
import com.example.electra.ui.theme.RojoError
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.theme.VerdeExito
import com.example.electra.ui.theme.VerdeSuave
import com.example.electra.ui.viewmodel.DetalleProtocoloViewModel
import com.example.electra.ui.viewmodel.MensajeProtocolo
import com.example.electra.validation.Validaciones

@Composable
fun DetalleProtocolo(
    viewModel: DetalleProtocoloViewModel,
    onBackClick: () -> Unit = {},
    onPruebaClick: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val protocolo = estado.protocolo

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargar()
    }

    LaunchedEffect(estado.mensaje) {
        val texto = when (estado.mensaje) {
            MensajeProtocolo.OBSERVACION_ACTUALIZADA -> "Observación actualizada"
            MensajeProtocolo.ERROR_ACTUALIZAR -> "Error al actualizar"
            MensajeProtocolo.FORMALIZADO -> {
                Vibracion.exito(context)
                "Protocolo formalizado"
            }
            null -> return@LaunchedEffect
        }
        Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()
        viewModel.mensajeMostrado()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(
            titulo = "Detalle del protocolo",
            subtitulo = protocolo?.numero ?: "Protocolo",
            onVolver = onBackClick,
            acciones = {
                if (protocolo != null) {
                    IconButton(onClick = {
                        viewModel.textoRespaldo()?.let {
                            Compartir.texto(context, "Protocolo ${protocolo.numero}", it)
                        }
                    }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Compartir con el cliente", tint = AzulPrincipal)
                    }
                }
            }
        )

        if (protocolo == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (estado.cargando) {
                    CircularProgressIndicator()
                } else {
                    Text(text = "No se encontró el protocolo.", color = TextoSecundario)
                }
            }
            return@Column
        }

        val formalizado = estado.formalizado
        val colorEstado by animateColorAsState(if (formalizado) VerdeExito else NaranjaPendiente, label = "colorEstado")
        val fondoEstado by animateColorAsState(if (formalizado) VerdeSuave else NaranjaSuave, label = "fondoEstado")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Tarjeta {
                Text(text = protocolo.numero, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                Text(text = "Equipo asociado: ${protocolo.equipo}", fontSize = 15.sp, color = AzulPrincipal)
                Row(
                    modifier = Modifier
                        .background(fondoEstado, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = formalizado,
                        transitionSpec = { scaleIn() togetherWith fadeOut() },
                        label = "iconoEstado"
                    ) { listo ->
                        Icon(
                            imageVector = if (listo) Icons.Outlined.Verified else Icons.Outlined.HourglassTop,
                            contentDescription = null,
                            tint = colorEstado,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(text = protocolo.estado, color = colorEstado, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }


            Tarjeta {
                Text("Información de certificación", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                FilaInfo(Icons.Outlined.CalendarToday, "Fecha de registro", protocolo.fecha)
                FilaInfo(Icons.Outlined.Person, "Responsable", protocolo.responsable)
                CatalogoEquipos.buscar(protocolo.equipo)?.let {
                    FilaInfo(Icons.Outlined.LocationCity, "Instalación", "${it.instalacion} · ${it.cliente}")
                }
            }


            Tarjeta {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Outlined.Comment, contentDescription = null, tint = AzulPrincipal, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Observaciones", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    }
                    IconButton(onClick = viewModel::abrirEdicion) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar observación", tint = AzulPrincipal)
                    }
                }
                Text(
                    text = protocolo.observaciones.ifBlank { "Sin observaciones registradas." },
                    fontSize = 14.sp,
                    color = if (protocolo.observaciones.isNotBlank()) AzulOscuro else TextoSecundario
                )
            }


            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Science, contentDescription = null, tint = AzulPrincipal, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pruebas del equipo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                }
                if (estado.pruebas.isEmpty()) {
                    Text("Aún no hay pruebas registradas para este equipo.", fontSize = 13.sp, color = TextoSecundario)
                }
                estado.pruebas.forEach { prueba ->
                    val aprobada = prueba.resultado == OpcionesPrueba.APROBADO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prueba.tipoPrueba, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AzulOscuro)
                            Text(
                                "${prueba.fecha} ${prueba.hora} · ${prueba.medicion} ${prueba.unidad}",
                                fontSize = 12.sp,
                                color = TextoSecundario
                            )
                        }
                        TextButton(onClick = { onPruebaClick(prueba.id) }) {
                            Text(prueba.resultado, color = if (aprobada) VerdeExito else RojoError)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !formalizado,
                exit = shrinkVertically() + fadeOut()
            ) {
                BotonCarga(
                    texto = "Formalizar protocolo",
                    icono = Icons.Filled.CheckCircle,
                    cargando = false,
                    onClick = viewModel::formalizar,
                    color = VerdeExito
                )
            }

            AnimatedVisibility(visible = formalizado, enter = fadeIn()) {
                Text(
                    text = "Protocolo formalizado. Puedes compartirlo con el cliente desde el ícono superior.",
                    fontSize = 13.sp,
                    color = TextoSecundario
                )
            }
        }
    }

    if (estado.editandoObservacion) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarEdicion,
            title = { Text("Editar observación", fontWeight = FontWeight.Bold, color = AzulOscuro) },
            text = {
                CampoValidado(
                    valor = estado.textoObservacion,
                    onCambio = viewModel::onObservacionChange,
                    etiqueta = "Observaciones / Notas",
                    icono = Icons.AutoMirrored.Outlined.Comment,
                    error = estado.errorObservacion,
                    singleLine = false,
                    minLines = 3,
                    maxCaracteres = Validaciones.MAX_OBSERVACIONES
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::guardarObservacion, enabled = estado.errorObservacion == null) {
                    Text("Guardar", color = AzulPrincipal, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarEdicion) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
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
        Column(
            modifier = Modifier
                .padding(18.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            contenido()
        }
    }
}

@Composable
private fun FilaInfo(icono: ImageVector, etiqueta: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = AzulPrincipal, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(etiqueta, fontSize = 12.sp, color = TextoSecundario)
            Text(valor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AzulOscuro)
        }
    }
}
