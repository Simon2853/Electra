package com.example.electra.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.electra.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Equipos(
    onEquipoClick: (String) -> Unit = {}
) {

    var busqueda by remember {
        mutableStateOf("")
    }

    var tipoSeleccionado by remember {
        mutableStateOf("Todos")
    }

    var filtroAbierto by remember {
        mutableStateOf(false)
    }

    // Datos de ejemplo
    val equipos = listOf(
        Equipo(
            codigo = "TR-001",
            tipo = "Transformador",
            cliente = "Empresa Eléctrica Norte",
            instalacion = "Subestación Norte"
        ),
        Equipo(
            codigo = "TR-002",
            tipo = "Transformador",
            cliente = "Industrias del Pacífico",
            instalacion = "Planta Industrial"
        ),
        Equipo(
            codigo = "RL-004",
            tipo = "Relé de protección",
            cliente = "Empresa Eléctrica Norte",
            instalacion = "Subestación Norte"
        ),
        Equipo(
            codigo = "CE-008",
            tipo = "Celda eléctrica",
            cliente = "Induztek Ingeniería",
            instalacion = "Subestación Central"
        ),
        Equipo(
            codigo = "CB-012",
            tipo = "Cable",
            cliente = "Industrias del Pacífico",
            instalacion = "Planta Industrial"
        )
    )

    // Filtrar equipos
    val equiposFiltrados = equipos.filter { equipo ->

        val coincideCodigo =
            equipo.codigo.contains(
                busqueda,
                ignoreCase = true
            )

        val coincideTipo =
            tipoSeleccionado == "Todos" ||
                    equipo.tipo == tipoSeleccionado

        coincideCodigo && coincideTipo
    }


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
                    horizontal = 20.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Outlined.Build,
                contentDescription = "Equipos",
                tint = Color(0xFF1769AA),
                modifier = Modifier.size(30.dp)
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column {

                Text(
                    text = "Equipos",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF173B57)
                )

                Text(
                    text = "Selecciona el equipo a revisar",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }


        // -------------------------------------------------
        // CONTENIDO
        // -------------------------------------------------

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // -------------------------------------------------
            // BUSCADOR
            // -------------------------------------------------

            item {

                OutlinedTextField(
                    value = busqueda,
                    onValueChange = {
                        busqueda = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text("Buscar por código")
                    },
                    placeholder = {
                        Text("Ej: TR-001")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Buscar"
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }


            // -------------------------------------------------
            // FILTRO POR TIPO
            // -------------------------------------------------

            item {

                ExposedDropdownMenuBox(
                    expanded = filtroAbierto,
                    onExpandedChange = {
                        filtroAbierto = !filtroAbierto
                    }
                ) {

                    OutlinedTextField(
                        value = tipoSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = {
                            Text("Filtrar por tipo")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = filtroAbierto
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = filtroAbierto,
                        onDismissRequest = {
                            filtroAbierto = false
                        }
                    ) {

                        val tipos = listOf(
                            "Todos",
                            "Transformador",
                            "Relé de protección",
                            "Celda eléctrica",
                            "Cable"
                        )

                        tipos.forEach { tipo ->

                            DropdownMenuItem(
                                text = {
                                    Text(tipo)
                                },
                                onClick = {

                                    tipoSeleccionado = tipo
                                    filtroAbierto = false
                                }
                            )
                        }
                    }
                }
            }


            // -------------------------------------------------
            // CONTADOR
            // -------------------------------------------------

            item {

                Text(
                    text = "${equiposFiltrados.size} equipos encontrados",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }


            // -------------------------------------------------
            // LISTA DE EQUIPOS
            // -------------------------------------------------

            items(equiposFiltrados) { equipo ->

                EquipoCard(
                    equipo = equipo,
                    onClick = {
                        onEquipoClick(equipo.codigo)
                    }
                )
            }


            // -------------------------------------------------
            // SIN RESULTADOS
            // -------------------------------------------------

            if (equiposFiltrados.isEmpty()) {

                item {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Sin resultados",
                            tint = Color.Gray,
                            modifier = Modifier.size(45.dp)
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text = "No se encontraron equipos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )

                        Text(
                            text = "Prueba con otro código o tipo.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}


// =====================================================
// TARJETA DE EQUIPO
// =====================================================

@Composable
fun EquipoCard(
    equipo: Equipo,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icono
            Card(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEAF3FA)
                )
            ) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Build,
                        contentDescription = "Equipo",
                        tint = Color(0xFF1769AA),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )


            // Información
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = equipo.codigo,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF173B57)
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = equipo.tipo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1769AA)
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = equipo.cliente,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )

                Text(
                    text = equipo.instalacion,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}


// =====================================================
// MODELO TEMPORAL
// =====================================================

data class Equipo(
    val codigo: String,
    val tipo: String,
    val cliente: String,
    val instalacion: String
)


// =====================================================
// PREVIEW
// =====================================================

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun EquiposPreview() {
    Equipos()
}