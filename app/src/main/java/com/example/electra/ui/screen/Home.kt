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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.electra.navigation.Routes

private val AzulElectra = Color(0xFF173B57)
private val AzulBoton = Color(0xFF1769AA)
private val FondoElectra = Color(0xFFF4F7FA)

@Composable
fun Home(
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoElectra)
    ) {

        // BARRA SUPERIOR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "ELECTRA",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulElectra
                )

                Text(
                    text = "Gestión de pruebas eléctricas",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = {
                    onNavigate(Routes.REGISTRAR_PRUEBA_PARAM.replace("{codigo}", "TR-001"))
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notificaciones",
                    tint = AzulElectra
                )
            }
        }

        // CONTENIDO PRINCIPAL
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // SALUDO
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Hola, Técnico",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulElectra
                    )

                    Text(
                        text = "Revisa el estado de tus pruebas y equipos.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // BOTÓN NUEVA PRUEBA
            item {
                Button(
                    onClick = {
                        onNavigate(Routes.REGISTRAR_PRUEBA_PARAM.replace("{codigo}", "TR-001"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulBoton
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Registrar nueva prueba",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // TÍTULO RESUMEN
            item {
                SectionTitle(text = "Resumen")
            }

            // ESTADÍSTICAS FILA 1
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Pruebas",
                        value = "24",
                        icon = Icons.Outlined.Assignment,
                        onClick = {
                            onNavigate(Routes.HISTORIAL_EQUIPO_PARAM.replace("{codigo}", "TR-001"))
                        }
                    )

                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Pendientes",
                        value = "5",
                        icon = Icons.Outlined.ListAlt,
                        onClick = {
                            onNavigate(Routes.PROTOCOLOS)
                        }
                    )
                }
            }

            // ESTADÍSTICAS FILA 2
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Protocolos",
                        value = "19",
                        icon = Icons.Outlined.AssignmentTurnedIn,
                        onClick = {
                            onNavigate(Routes.PROTOCOLOS)
                        }
                    )

                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Equipos",
                        value = "32",
                        icon = Icons.Outlined.Build,
                        onClick = {
                            onNavigate(Routes.EQUIPOS)
                        }
                    )
                }
            }

            // TÍTULO ACCESOS RÁPIDOS
            item {
                SectionTitle(text = "Accesos rápidos")
            }

            // ACCIONES FILA 1
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAction(
                        modifier = Modifier.weight(1f),
                        title = "Equipos",
                        icon = Icons.Outlined.Build,
                        onClick = {
                            onNavigate(Routes.EQUIPOS)
                        }
                    )

                    QuickAction(
                        modifier = Modifier.weight(1f),
                        title = "Historial",
                        icon = Icons.Outlined.History,
                        onClick = {
                            onNavigate(Routes.HISTORIAL_EQUIPO_PARAM.replace("{codigo}", "TR-001"))
                        }
                    )
                }
            }

            // ACCIONES FILA 2
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAction(
                        modifier = Modifier.weight(1f),
                        title = "Pruebas",
                        icon = Icons.Outlined.Assignment,
                        onClick = {
                            onNavigate(Routes.REGISTRAR_PRUEBA_PARAM.replace("{codigo}", "TR-001"))
                        }
                    )

                    QuickAction(
                        modifier = Modifier.weight(1f),
                        title = "Protocolos",
                        icon = Icons.Outlined.AssignmentTurnedIn,
                        onClick = {
                            onNavigate(Routes.PROTOCOLOS)
                        }
                    )
                }
            }

            // ACTIVIDAD RECIENTE
            item {
                SectionTitle(
                    text = "Actividad reciente",
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // LISTA DE ACTIVIDADES
            items(
                listOf(
                    "Prueba registrada - TR-001",
                    "Protocolo generado - PR-018",
                    "Prueba registrada - RL-004"
                )
            ) { actividad ->
                ActivityItem(
                    text = actividad,
                    onClick = {
                        if (actividad.startsWith("Prueba registrada")) {
                            val codigo = actividad.substringAfter("Prueba registrada - ").trim()
                            onNavigate(Routes.HISTORIAL_EQUIPO_PARAM.replace("{codigo}", codigo))
                        } else if (actividad.startsWith("Protocolo generado")) {
                            onNavigate(Routes.PROTOCOLOS)
                        }
                    }
                )
            }

            // PIE DE PÁGINA
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Induztek Ingeniería SpA",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Gestión de pruebas eléctricas",
                        modifier = Modifier.padding(top = 3.dp),
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// TÍTULOS DE SECCIÓN
@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = AzulElectra
    )
}

// TARJETA DE ESTADÍSTICA
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AzulBoton,
                modifier = Modifier.size(28.dp)
            )

            Text(
                text = value,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = AzulElectra
            )

            Text(
                text = title,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

// ACCESO RÁPIDO
@Composable
fun QuickAction(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
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
                .padding(vertical = 18.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AzulBoton,
                modifier = Modifier.size(30.dp)
            )

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AzulElectra,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ACTIVIDAD RECIENTE
@Composable
fun ActivityItem(
    text: String,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = "Actividad",
                tint = AzulBoton,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                color = Color(0xFF333333)
            )
        }
    }
}

// VISTA PREVIA
@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun HomePreview() {
    Home(
        onNavigate = {}
    )
}