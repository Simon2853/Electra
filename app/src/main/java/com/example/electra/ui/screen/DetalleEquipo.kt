package com.example.electra.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import androidx.compose.foundation.clickable


@Composable
fun DetalleEquipo(
    codigoEquipo: String,
    onVolverClick: () -> Unit = {},
    onNuevaPruebaClick: () -> Unit = {},
    onHistorialClick: () -> Unit = {},
    onProtocolosClick: () -> Unit = {}
) {
    // Conserva aquí el contenido actual de tu pantalla.
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FA))
    ) {

// BARRA SUPERIOR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    onVolverClick()
                },
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF173B57)
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Detalle del equipo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF173B57)
                )

                Text(
                    text = "Información del equipo",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }


        // CONTENIDO
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ENCABEZADO DEL EQUIPO
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 3.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            modifier = Modifier.size(58.dp),
                            shape = RoundedCornerShape(12.dp),
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
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "TR-001",
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF173B57)
                            )

                            Text(
                                text = "Transformador",
                                fontSize = 14.sp,
                                color = Color(0xFF1769AA),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // INFORMACIÓN DEL EQUIPO
            Text(
                text = "Información",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173B57)
            )

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    InfoRow(
                        icon = Icons.Outlined.Build,
                        label = "Código",
                        value = "TR-001"
                    )

                    InfoRow(
                        icon = Icons.Outlined.Assignment,
                        label = "Tipo",
                        value = "Transformador"
                    )

                    InfoRow(
                        icon = Icons.Outlined.Person,
                        label = "Cliente",
                        value = "Empresa Eléctrica Norte"
                    )

                    InfoRow(
                        icon = Icons.Outlined.Place,
                        label = "Instalación",
                        value = "Subestación Norte"
                    )

                    InfoRow(
                        icon = Icons.Outlined.LocationOn,
                        label = "Ubicación",
                        value = "Santiago, Región Metropolitana"
                    )

                    InfoRow(
                        icon = Icons.Outlined.CalendarToday,
                        label = "Última prueba",
                        value = "15/09/2026"
                    )
                }
            }

            // ESTADO
            Text(
                text = "Estado",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173B57)
            )

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
                    Card(
                        modifier = Modifier.size(12.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2E7D32)
                        )
                    ) {}

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Operativo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )

                        Text(
                            text = "Equipo disponible para realizar pruebas",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // ACCIONES
            Text(
                text = "Acciones",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173B57)
            )

            Button(
                onClick = onNuevaPruebaClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1769AA)
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlaylistAdd,
                    contentDescription = "Nueva prueba"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Nueva prueba",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onHistorialClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = "Ver historial"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Ver historial",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            OutlinedButton(
                onClick = onProtocolosClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AssignmentTurnedIn,
                    contentDescription = "Ver protocolos"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Ver protocolos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// FILA DE INFORMACIÓN
@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF1769AA),
            modifier = Modifier.size(25.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF173B57)
            )
        }
    }
}


@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun DetalleEquipoPreview() {
    DetalleEquipo(
        codigoEquipo = "TR-001"
    )
}
