package com.example.electra.ui.screen

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.data.ProtocolosRepositoryEnMemoria
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.OpcionesPrueba
import com.example.electra.model.Prueba
import com.example.electra.navigation.Routes
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.components.BotonCarga
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.NaranjaPendiente
import com.example.electra.ui.theme.RojoError
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.theme.VerdeExito
import com.example.electra.ui.viewmodel.HomeViewModel

@Composable
fun Home(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val equipoPorDefecto = CatalogoEquipos.CODIGOS.first()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargar()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(
            titulo = "ELECTRA",
            subtitulo = "Gestión de pruebas eléctricas",
            acciones = {
                IconButton(onClick = { onNavigate(Routes.PERFIL) }) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Perfil",
                        tint = AzulOscuro,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Hola, ${estado.nombre}",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulOscuro
                    )
                    Text(
                        text = "Revisa el estado de tus pruebas y equipos.",
                        fontSize = 14.sp,
                        color = TextoSecundario
                    )
                }
            }

            item {
                BotonCarga(
                    texto = "Registrar nueva prueba",
                    icono = Icons.Outlined.Add,
                    cargando = false,
                    onClick = { onNavigate(Routes.registrarPrueba(equipoPorDefecto)) }
                )
            }

            item { SectionTitle(text = "Resumen") }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Pruebas",
                        value = estado.totalPruebas,
                        icon = Icons.AutoMirrored.Outlined.Assignment,
                        onClick = { onNavigate(Routes.historialEquipo(equipoPorDefecto)) }
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Pendientes",
                        value = estado.protocolosPendientes,
                        icon = Icons.Outlined.PendingActions,
                        tint = NaranjaPendiente,
                        onClick = { onNavigate(Routes.PROTOCOLOS) }
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Protocolos",
                        value = estado.totalProtocolos,
                        icon = Icons.Outlined.AssignmentTurnedIn,
                        onClick = { onNavigate(Routes.PROTOCOLOS) }
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Equipos",
                        value = estado.totalEquipos,
                        icon = Icons.Outlined.Build,
                        onClick = { onNavigate(Routes.EQUIPOS) }
                    )
                }
            }

            item { SectionTitle(text = "Accesos rápidos") }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAction(
                        modifier = Modifier.weight(1f),
                        title = "Equipos",
                        icon = Icons.Outlined.Build,
                        onClick = { onNavigate(Routes.EQUIPOS) }
                    )
                    QuickAction(
                        modifier = Modifier.weight(1f),
                        title = "Historial",
                        icon = Icons.Outlined.History,
                        onClick = { onNavigate(Routes.historialEquipo(equipoPorDefecto)) }
                    )
                    QuickAction(
                        modifier = Modifier.weight(1f),
                        title = "Protocolos",
                        icon = Icons.Outlined.AssignmentTurnedIn,
                        onClick = { onNavigate(Routes.PROTOCOLOS) }
                    )
                }
            }

            item { SectionTitle(text = "Actividad reciente", modifier = Modifier.padding(top = 4.dp)) }

            if (estado.recientes.isEmpty()) {
                item {
                    Text(
                        text = "Aún no registras pruebas. Usa \"Registrar nueva prueba\" para comenzar.",
                        fontSize = 13.sp,
                        color = TextoSecundario
                    )
                }
            }

            items(estado.recientes, key = { it.id }) { prueba ->
                ActivityItem(
                    prueba = prueba,
                    modifier = Modifier.animateItem(),
                    onClick = { onNavigate(Routes.detallePrueba(prueba.id)) }
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Induztek Ingeniería SpA", fontSize = 12.sp, color = TextoSecundario, textAlign = TextAlign.Center)
                    Text(
                        text = "Gestión de pruebas eléctricas",
                        modifier = Modifier.padding(top = 3.dp),
                        fontSize = 11.sp,
                        color = TextoSecundario,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = AzulOscuro
    )
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: Int,
    icon: ImageVector,
    tint: Color = AzulPrincipal,
    onClick: () -> Unit = {}
) {
    val animado by animateIntAsState(targetValue = value, animationSpec = tween(800), label = "contador_$title")

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(28.dp))
            Text(text = animado.toString(), fontSize = 25.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
            Text(text = title, fontSize = 13.sp, color = TextoSecundario)
        }
    }
}

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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = AzulPrincipal, modifier = Modifier.size(28.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AzulOscuro,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActivityItem(
    prueba: Prueba,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val aprobada = prueba.resultado == OpcionesPrueba.APROBADO

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (aprobada) Icons.Filled.CheckCircle else Icons.Filled.Error,
                contentDescription = prueba.resultado,
                tint = if (aprobada) VerdeExito else RojoError,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Prueba registrada - ${prueba.equipo}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulOscuro
                )
                Text(
                    text = "${prueba.tipoPrueba} · ${prueba.fecha} ${prueba.hora}",
                    fontSize = 12.sp,
                    color = TextoSecundario
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomePreview() {
    ElectraTheme {
        Home(
            viewModel = viewModel { HomeViewModel(
                PruebasRepositoryEnMemoria(),
                ProtocolosRepositoryEnMemoria(),
                UsuariosRepositoryEnMemoria(),
                SesionEnMemoria()
            ) },
            onNavigate = {}
        )
    }
}
