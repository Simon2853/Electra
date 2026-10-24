package com.example.electra.ui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Save
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.Prueba
import com.example.electra.ui.viewmodel.RegistrarPruebaViewModel
import com.example.electra.ui.viewmodel.ResultadoGuardado
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarPrueba(
    viewModel: RegistrarPruebaViewModel,
    codigoEquipo: String = "TR-001",
    onVolverClick: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    // -------------------------------------------------
    // ESTADOS TEMPORALES
    // -------------------------------------------------

    var equipo by remember {
        mutableStateOf(codigoEquipo)
    }

    var tipoPrueba by remember {
        mutableStateOf("Resistencia de aislamiento")
    }

    var medicion by remember {
        mutableStateOf("")
    }

    var unidad by remember {
        mutableStateOf("MΩ")
    }

    var resultado by remember {
        mutableStateOf("Aprobado")
    }

    var observaciones by remember {
        mutableStateOf("")
    }

    var equipoMenuAbierto by remember {
        mutableStateOf(false)
    }

    var tipoPruebaMenuAbierto by remember {
        mutableStateOf(false)
    }

    var unidadMenuAbierta by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val calendario = remember {
        Calendar.getInstance()
    }

    var fechaSeleccionada by remember {
        mutableStateOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(calendario.time)
        )
    }

    var horaSeleccionada by remember {
        mutableStateOf(
            SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(calendario.time)
        )
    }

    var mensajeGuardado by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(estado.resultado) {
        val resultadoGuardado = estado.resultado
        val texto = when (resultadoGuardado) {
            ResultadoGuardado.MEDICION_VACIA -> "Ingresa una medición"
            ResultadoGuardado.ERROR -> "No se pudo guardar la prueba"
            ResultadoGuardado.EXITO -> "Prueba guardada correctamente"
            null -> return@LaunchedEffect
        }
        Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()
        viewModel.resultadoMostrado()

        if (resultadoGuardado == ResultadoGuardado.EXITO) {
            mensajeGuardado = true
            // Regresa a la pantalla anterior para ver la prueba reflejada
            onVolverClick()
        }
    }

    // -------------------------------------------------
    // PANTALLA
    // -------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FA))
    ) {

        // -------------------------------------------------
        // BARRA SUPERIOR
        // -------------------------------------------------

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onVolverClick
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Volver al menú anterior",
                    tint = Color(0xFF173B57)
                )
            }

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Column {

                Text(
                    text = "Registrar prueba",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF173B57)
                )

                Text(
                    text = "Registro de prueba en terreno",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }


        // -------------------------------------------------
        // CONTENIDO
        // -------------------------------------------------

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ENCABEZADO
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Assignment,
                        contentDescription = "Prueba",
                        tint = Color(0xFF1769AA),
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Column {

                        Text(
                            text = "Nueva prueba",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF173B57)
                        )

                        Text(
                            text = "Ingresa los resultados obtenidos",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // EQUIPO
            Text(
                text = "Equipo",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173B57)
            )

            ExposedDropdownMenuBox(
                expanded = equipoMenuAbierto,
                onExpandedChange = {
                    equipoMenuAbierto = !equipoMenuAbierto
                }
            ) {

                OutlinedTextField(
                    value = equipo,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = equipoMenuAbierto
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = equipoMenuAbierto,
                    onDismissRequest = {
                        equipoMenuAbierto = false
                    }
                ) {

                    CatalogoEquipos.CODIGOS_POR_DEFECTO.forEach { equipoDisponible ->

                        DropdownMenuItem(
                            text = {
                                Text(equipoDisponible)
                            },
                            onClick = {
                                equipo = equipoDisponible
                                equipoMenuAbierto = false
                            }
                        )
                    }
                }
            }

            // TIPO DE PRUEBA
            Text(
                text = "Tipo de prueba",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173B57)
            )

            ExposedDropdownMenuBox(
                expanded = tipoPruebaMenuAbierto,
                onExpandedChange = {
                    tipoPruebaMenuAbierto = !tipoPruebaMenuAbierto
                }
            ) {

                OutlinedTextField(
                    value = tipoPrueba,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = tipoPruebaMenuAbierto
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = tipoPruebaMenuAbierto,
                    onDismissRequest = {
                        tipoPruebaMenuAbierto = false
                    }
                ) {

                    listOf(
                        "Resistencia de aislamiento",
                        "Resistencia de puesta a tierra",
                        "Relación de transformación",
                        "Prueba de continuidad"
                    ).forEach { prueba ->

                        DropdownMenuItem(
                            text = {
                                Text(prueba)
                            },
                            onClick = {
                                tipoPrueba = prueba
                                tipoPruebaMenuAbierto = false
                            }
                        )
                    }
                }
            }

            // FECHA Y HORA
            Text(
                text = "Fecha y hora de la prueba",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173B57)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendario.set(
                                    year,
                                    month,
                                    day,
                                    calendario.get(Calendar.HOUR_OF_DAY),
                                    calendario.get(Calendar.MINUTE)
                                )

                                fechaSeleccionada = String.format(
                                    Locale.getDefault(),
                                    "%02d/%02d/%04d",
                                    day,
                                    month + 1,
                                    year
                                )

                                mensajeGuardado = false
                            },
                            calendario.get(Calendar.YEAR),
                            calendario.get(Calendar.MONTH),
                            calendario.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Fecha: $fechaSeleccionada",
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                calendario.set(
                                    calendario.get(Calendar.YEAR),
                                    calendario.get(Calendar.MONTH),
                                    calendario.get(Calendar.DAY_OF_MONTH),
                                    hour,
                                    minute
                                )

                                horaSeleccionada = String.format(
                                    Locale.getDefault(),
                                    "%02d:%02d",
                                    hour,
                                    minute
                                )

                                mensajeGuardado = false
                            },
                            calendario.get(Calendar.HOUR_OF_DAY),
                            calendario.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Hora: $horaSeleccionada",
                        fontSize = 12.sp
                    )
                }
            }

            // MEDICIÓN
            Text(
                text = "Medición",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173B57)
            )

            OutlinedTextField(
                value = medicion,
                onValueChange = {
                    medicion = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text("Ej: 125.5")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // UNIDAD
            Text(
                text = "Unidad de medida",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173B57)
            )

            ExposedDropdownMenuBox(
                expanded = unidadMenuAbierta,
                onExpandedChange = {
                    unidadMenuAbierta = !unidadMenuAbierta
                }
            ) {

                OutlinedTextField(
                    value = unidad,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = unidadMenuAbierta
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = unidadMenuAbierta,
                    onDismissRequest = {
                        unidadMenuAbierta = false
                    }
                ) {

                    listOf(
                        "MΩ",
                        "Ω",
                        "kΩ",
                        "V",
                        "A"
                    ).forEach { unidadDisponible ->

                        DropdownMenuItem(
                            text = {
                                Text(unidadDisponible)
                            },
                            onClick = {
                                unidad = unidadDisponible
                                unidadMenuAbierta = false
                            }
                        )
                    }
                }
            }

            // RESULTADO
            Text(
                text = "Resultado",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173B57)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        RadioButton(
                            selected = resultado == "Aprobado",
                            onClick = {
                                resultado = "Aprobado"
                            }
                        )

                        Text(
                            text = "Aprobado",
                            fontSize = 14.sp
                        )
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        RadioButton(
                            selected = resultado == "Rechazado",
                            onClick = {
                                resultado = "Rechazado"
                            }
                        )

                        Text(
                            text = "Rechazado",
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // OBSERVACIONES
            Text(
                text = "Observaciones",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173B57)
            )

            OutlinedTextField(
                value = observaciones,
                onValueChange = {
                    observaciones = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = {
                    Text(
                        "Ingresa observaciones de la prueba..."
                    )
                },
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            // GUARDAR PRUEBA
            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    viewModel.guardar(
                        Prueba(
                            equipo = equipo,
                            tipoPrueba = tipoPrueba,
                            fecha = fechaSeleccionada,
                            hora = horaSeleccionada,
                            medicion = medicion,
                            unidad = unidad,
                            resultado = resultado,
                            observaciones = observaciones
                        )
                    )
                },
                enabled = !estado.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1769AA)
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Guardar prueba")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CONFIRMACIÓN Y RESUMEN
            if (mensajeGuardado) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Prueba registrada correctamente",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Text("Equipo: $equipo")
                        Text("Tipo de prueba: $tipoPrueba")
                        Text("Fecha: $fechaSeleccionada")
                        Text("Hora: $horaSeleccionada")
                        Text("Medición: $medicion $unidad")
                        Text("Resultado: $resultado")

                        if (observaciones.isNotBlank()) {
                            Text(
                                text = "Observaciones: $observaciones"
                            )
                        }
                    }
                }
            } else if (medicion.isBlank()) {
                Text(
                    text = "Ingresa una medición antes de guardar.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// PREVIEW
@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun RegistrarPruebaPreview() {
    androidx.compose.material3.MaterialTheme {
        RegistrarPrueba(viewModel = RegistrarPruebaViewModel(PruebasRepositoryEnMemoria()))
    }
}