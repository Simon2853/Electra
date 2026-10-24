package com.example.electra.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.ui.viewmodel.HistorialEquipoViewModel

private val BackgroundColor = Color(0xFFF4F7FA)
private val DarkBlue = Color(0xFF173B57)
private val MainBlue = Color(0xFF1769AA)
private val SuccessGreen = Color(0xFF2E7D32)

data class RegistroHistorial(
    val id: Long,
    val fecha: String,
    val medicion: String,
    val resultado: String,
    val tipoPrueba: String,
    val observaciones: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialEquipo(
    viewModel: HistorialEquipoViewModel,
    onBackClick: () -> Unit = {},
    onPruebaClick: (Long) -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val equipoSeleccionado = estado.equipoSeleccionado
    val listaEquipos = estado.equipos
    var menuEquiposAbierto by remember { mutableStateOf(false) }

    val historial = remember(estado.pruebas) {
        estado.pruebas.map { prueba ->
            RegistroHistorial(
                id = prueba.id,
                fecha = "${prueba.fecha} ${prueba.hora}",
                medicion = "${prueba.medicion} ${prueba.unidad}",
                resultado = prueba.resultado.uppercase(),
                tipoPrueba = "${prueba.equipo} - ${prueba.tipoPrueba}",
                observaciones = prueba.observaciones
            )
        }
    }

    // Recarga al entrar y al volver de Registrar prueba / Detalle
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargar()
    }

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
                    text = "Historial del equipo",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado interactivo
            item {
                ExposedDropdownMenuBox(
                    expanded = menuEquiposAbierto,
                    onExpandedChange = { menuEquiposAbierto = !menuEquiposAbierto }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
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
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MainBlue
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "HISTORIAL — $equipoSeleccionado",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBlue
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Toca para cambiar de equipo",
                                    fontSize = 13.sp,
                                    color = MainBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuEquiposAbierto)
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = menuEquiposAbierto,
                        onDismissRequest = { menuEquiposAbierto = false }
                    ) {
                        listaEquipos.forEach { equipo ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = equipo,
                                        fontWeight = if (equipo == equipoSeleccionado) FontWeight.Bold else FontWeight.Normal,
                                        color = if (equipo == equipoSeleccionado) MainBlue else DarkBlue
                                    )
                                },
                                onClick = {
                                    viewModel.seleccionarEquipo(equipo)
                                    menuEquiposAbierto = false
                                }
                            )
                        }
                    }
                }
            }

            // Resumen de mediciones
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Resumen de mediciones — $equipoSeleccionado",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Cantidad de pruebas: ${estado.totalPruebas}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (historial.isNotEmpty()) {
                            Text(
                                text = "Última medición: ${historial.first().medicion}",
                                fontSize = 14.sp,
                                color = DarkBlue
                            )

                            Text(
                                text = "Fecha: ${historial.first().fecha}",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        } else {
                            Text(
                                text = "Aún no hay mediciones guardadas para este equipo.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Título de registros
            item {
                Column {
                    Text(
                        text = "Pruebas anteriores (Toca para ver detalle)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )

                    if (estado.totalPruebas > historial.size) {
                        Text(
                            text = "Mostrando las últimas ${historial.size} de ${estado.totalPruebas}",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Lista de tarjetas
            if (historial.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "No hay pruebas registradas para $equipoSeleccionado.",
                            modifier = Modifier.padding(20.dp),
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(historial) { registro ->
                    HistorialCard(
                        registro = registro,
                        onClick = { onPruebaClick(registro.id) }
                    )
                }
            }

            item {
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

@Composable
private fun HistorialCard(
    registro: RegistroHistorial,
    onClick: () -> Unit = {}
) {
    val aprobado = registro.resultado.equals("APROBADO", ignoreCase = true)
    val colorResultado = if (aprobado) SuccessGreen else Color(0xFFC62828)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = registro.fecha,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainBlue
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = registro.tipoPrueba,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = registro.medicion,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorResultado
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = registro.resultado,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResultado
                    )
                }
            }

            if (registro.observaciones.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Observaciones: ${registro.observaciones}",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HistorialEquipoPreview() {
    MaterialTheme {
        HistorialEquipo(viewModel = HistorialEquipoViewModel(PruebasRepositoryEnMemoria(), "TR-001"))
    }
}