package com.example.electra.ui.screen

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.data.ProtocolosRepositoryEnMemoria
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.ui.viewmodel.CrearProtocoloViewModel
import com.example.electra.ui.viewmodel.ResultadoProtocolo

private val BackgroundColor = Color(0xFFF4F7FA)
private val DarkBlue = Color(0xFF173B57)
private val MainBlue = Color(0xFF1769AA)
private val SuccessGreen = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearProtocolo(
    viewModel: CrearProtocoloViewModel,
    onBackClick: () -> Unit = {},
    onProtocoloGuardado: () -> Unit = {}
) {
    val context = LocalContext.current
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    var equipoExpanded by remember { mutableStateOf(false) }
    var estadoExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(estado.resultado) {
        when (estado.resultado) {
            ResultadoProtocolo.CAMPOS_INCOMPLETOS ->
                Toast.makeText(context, "Por favor completa el número y el responsable", Toast.LENGTH_SHORT).show()

            ResultadoProtocolo.DUPLICADO ->
                Toast.makeText(context, "Error al guardar el protocolo (Número duplicado)", Toast.LENGTH_SHORT).show()

            ResultadoProtocolo.EXITO -> {
                Toast.makeText(context, "Protocolo guardado con éxito", Toast.LENGTH_SHORT).show()
                onProtocoloGuardado()
            }

            null -> return@LaunchedEffect
        }
        viewModel.resultadoMostrado()
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
                    text = "Nuevo Protocolo",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Datos del Protocolo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )

                    // Número de Protocolo
                    OutlinedTextField(
                        value = estado.numero,
                        onValueChange = viewModel::onNumeroChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Número de Protocolo") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Assignment, contentDescription = null, tint = MainBlue)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // Selector de Equipo
                    ExposedDropdownMenuBox(
                        expanded = equipoExpanded,
                        onExpandedChange = { equipoExpanded = !equipoExpanded }
                    ) {
                        OutlinedTextField(
                            value = estado.equipo,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            label = { Text("Equipo Asignado") },
                            leadingIcon = {
                                Icon(Icons.Outlined.PrecisionManufacturing, contentDescription = null, tint = MainBlue)
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = equipoExpanded)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = equipoExpanded,
                            onDismissRequest = { equipoExpanded = false }
                        ) {
                            estado.equipos.forEach { eq ->
                                DropdownMenuItem(
                                    text = { Text(eq) },
                                    onClick = {
                                        viewModel.onEquipoChange(eq)
                                        equipoExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Fecha de emisión
                    OutlinedTextField(
                        value = estado.fecha,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Fecha de Emisión") },
                        leadingIcon = {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MainBlue)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // Estado inicial
                    ExposedDropdownMenuBox(
                        expanded = estadoExpanded,
                        onExpandedChange = { estadoExpanded = !estadoExpanded }
                    ) {
                        OutlinedTextField(
                            value = estado.estado,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            label = { Text("Estado del Protocolo") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = estadoExpanded,
                            onDismissRequest = { estadoExpanded = false }
                        ) {
                            listOf("Registrado en terreno", "Formalizado").forEach { est ->
                                DropdownMenuItem(
                                    text = { Text(est) },
                                    onClick = {
                                        viewModel.onEstadoChange(est)
                                        estadoExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Responsable
                    OutlinedTextField(
                        value = estado.responsable,
                        onValueChange = viewModel::onResponsableChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Responsable / Inspector") },
                        placeholder = { Text("Ej: Juan Pérez") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = MainBlue)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // Observaciones / notas
                    OutlinedTextField(
                        value = estado.observaciones,
                        onValueChange = viewModel::onObservacionesChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Observaciones / Notas") },
                        placeholder = { Text("Escribe las notas del protocolo aquí...") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Comment, contentDescription = null, tint = MainBlue)
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }

            Button(
                onClick = viewModel::guardar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guardar Protocolo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CrearProtocoloPreview() {
    MaterialTheme {
        CrearProtocolo(
            viewModel = CrearProtocoloViewModel(ProtocolosRepositoryEnMemoria(), PruebasRepositoryEnMemoria())
        )
    }
}