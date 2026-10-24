package com.example.electra.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.ui.viewmodel.DetalleProtocoloViewModel
import com.example.electra.ui.viewmodel.MensajeProtocolo

private val BackgroundColor = Color(0xFFF4F7FA)
private val DarkBlue = Color(0xFF173B57)
private val MainBlue = Color(0xFF1769AA)
private val SuccessGreen = Color(0xFF2E7D32)

@Composable
fun DetalleProtocolo(
    viewModel: DetalleProtocoloViewModel,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val protocolo = estado.protocolo

    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var textoEditarObservacion by remember { mutableStateOf("") }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargar()
    }

    LaunchedEffect(estado.mensaje) {
        val texto = when (estado.mensaje) {
            MensajeProtocolo.OBSERVACION_ACTUALIZADA -> "Observación actualizada"
            MensajeProtocolo.ERROR_ACTUALIZAR -> "Error al actualizar"
            null -> return@LaunchedEffect
        }
        Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()
        viewModel.mensajeMostrado()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Barra Superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = DarkBlue)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("ELECTRA", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                Text("Detalle del protocolo", fontSize = 13.sp, color = Color.Gray)
            }
        }

        if (protocolo == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (estado.cargando) "Cargando información del protocolo..." else "No se encontró el protocolo.",
                    color = Color.Gray
                )
            }
        } else {
            val p = protocolo
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta 1: Encabezado y Estado
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (p.numero.startsWith("#")) p.numero else "#${p.numero}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Equipo asociado: ${p.equipo}", fontSize = 15.sp, color = MainBlue)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Estado actual: ${p.estado}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (p.estado.equals("Formalizado", ignoreCase = true)) SuccessGreen else Color(0xFFE68A00)
                        )
                    }
                }

                // Tarjeta 2: Información General
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Información de Certificación", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkBlue)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MainBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Fecha de registro: ${p.fecha}", fontSize = 14.sp, color = DarkBlue)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = MainBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Responsable: ${p.responsable}", fontSize = 14.sp, color = DarkBlue)
                        }
                    }
                }

                // Tarjeta 3: Observaciones y Notas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Comment,
                                    contentDescription = null,
                                    tint = MainBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Observaciones",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBlue
                                )
                            }

                            IconButton(
                                onClick = {
                                    textoEditarObservacion = p.observaciones
                                    mostrarDialogoEditar = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar Observación",
                                    tint = MainBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (p.observaciones.isNotBlank()) p.observaciones else "Sin observaciones registradas.",
                            fontSize = 14.sp,
                            color = if (p.observaciones.isNotBlank()) DarkBlue else Color.Gray
                        )
                    }
                }

                // Botón de Acción
                if (!p.estado.equals("Formalizado", ignoreCase = true)) {
                    Button(
                        onClick = viewModel::formalizar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Formalizar Protocolo")
                    }
                }
            }
        }
    }

    // Diálogo modal para editar la observación en tiempo real
    if (mostrarDialogoEditar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEditar = false },
            title = { Text("Editar Observación", fontWeight = FontWeight.Bold, color = DarkBlue) },
            text = {
                OutlinedTextField(
                    value = textoEditarObservacion,
                    onValueChange = { textoEditarObservacion = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Observaciones / Notas") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.actualizarObservaciones(textoEditarObservacion)
                        mostrarDialogoEditar = false
                    }
                ) {
                    Text("Guardar", color = MainBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEditar = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}