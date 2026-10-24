package com.example.electra.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.model.Prueba
import com.example.electra.ui.viewmodel.DetallePruebaViewModel

private val BackgroundColor = Color(0xFFF4F7FA)
private val DarkBlue = Color(0xFF173B57)
private val MainBlue = Color(0xFF1769AA)
private val SuccessGreen = Color(0xFF2E7D32)
private val ErrorRed = Color(0xFFC62828)

@Composable
fun DetallePrueba(
    idPrueba: Long,
    viewModel: DetallePruebaViewModel,
    onBackClick: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val prueba = estado.prueba

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = DarkBlue
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "ELECTRA",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )

                Text(
                    text = "Detalle de la prueba",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        if (prueba == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = if (estado.cargando) "Cargando prueba..." else "No se encontró la prueba solicitada (ID: $idPrueba).",
                        modifier = Modifier.padding(24.dp),
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val p = prueba
            val aprobado = p.resultado.equals("APROBADO", ignoreCase = true)
            val colorEstado = if (aprobado) SuccessGreen else ErrorRed

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Encabezado con estado (Aprobado / Rechazado)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (aprobado) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = colorEstado,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = p.equipo,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBlue
                            )

                            Text(
                                text = "Estado: ${p.resultado.uppercase()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorEstado
                            )
                        }
                    }
                }

                // Datos de la prueba
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Información General",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )

                        // Tipo de prueba
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Assignment,
                                contentDescription = null,
                                tint = MainBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Tipo de prueba", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    text = p.tipoPrueba,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkBlue
                                )
                            }
                        }

                        // Fecha y hora
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = MainBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Fecha y Hora", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    text = "${p.fecha} a las ${p.hora}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkBlue
                                )
                            }
                        }

                        // Medición obtenida
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Speed,
                                contentDescription = null,
                                tint = MainBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Medición registrada", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    text = "${p.medicion} ${p.unidad}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBlue
                                )
                            }
                        }
                    }
                }

                // Observaciones
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (p.observaciones.isNotBlank()) p.observaciones else "Sin observaciones registradas.",
                            fontSize = 14.sp,
                            color = if (p.observaciones.isNotBlank()) DarkBlue else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ELECTRA • Induztek Ingeniería SpA",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DetallePruebaPreview() {
    MaterialTheme {
        val repositorio = PruebasRepositoryEnMemoria(
            listOf(
                Prueba(
                    id = 1, equipo = "TR-001", tipoPrueba = "Resistencia de aislamiento",
                    fecha = "23/10/2026", hora = "10:30", medicion = "1200", unidad = "MΩ",
                    resultado = "Aprobado", observaciones = ""
                )
            )
        )
        DetallePrueba(idPrueba = 1L, viewModel = DetallePruebaViewModel(repositorio, 1L))
    }
}