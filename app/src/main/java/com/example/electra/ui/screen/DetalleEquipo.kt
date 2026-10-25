package com.example.electra.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.electra.model.CatalogoEquipos
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.components.BotonCarga
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.AzulSuave
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.TextoSecundario

@Composable
fun DetalleEquipo(
    codigoEquipo: String,
    onVolverClick: () -> Unit = {},
    onNuevaPruebaClick: () -> Unit = {},
    onHistorialClick: () -> Unit = {},
    onProtocolosClick: () -> Unit = {}
) {
    val equipo = CatalogoEquipos.buscar(codigoEquipo)

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(titulo = "Detalle del equipo", subtitulo = "Información del equipo", onVolver = onVolverClick)

        if (equipo == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No se encontró el equipo $codigoEquipo.",
                    color = TextoSecundario,
                    textAlign = TextAlign.Center
                )
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 4 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(AzulSuave, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Build, contentDescription = null, tint = AzulPrincipal, modifier = Modifier.size(30.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(equipo.codigo, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                                Text(equipo.tipo, fontSize = 15.sp, color = AzulPrincipal)
                            }
                        }
                        InfoRow(Icons.Outlined.Category, "Tipo de equipo", equipo.tipo)
                        InfoRow(Icons.Outlined.Business, "Cliente", equipo.cliente)
                        InfoRow(Icons.Outlined.LocationCity, "Instalación", equipo.instalacion)
                    }
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 150)) + slideInVertically(tween(400, delayMillis = 150)) { it / 4 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Acciones", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)

                    BotonCarga(
                        texto = "Registrar nueva prueba",
                        icono = Icons.Outlined.Add,
                        cargando = false,
                        onClick = onNuevaPruebaClick
                    )
                    BotonSecundario("Ver historial de pruebas", Icons.Outlined.History, onHistorialClick)
                    BotonSecundario("Ver protocolos", Icons.Outlined.AssignmentTurnedIn, onProtocolosClick)
                }
            }
        }
    }
}

@Composable
private fun BotonSecundario(texto: String, icono: ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icono, contentDescription = null, tint = AzulPrincipal)
        Spacer(Modifier.width(8.dp))
        Text(texto, color = AzulPrincipal, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun InfoRow(icono: ImageVector, etiqueta: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = AzulPrincipal, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(etiqueta, fontSize = 12.sp, color = TextoSecundario)
            Text(valor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AzulOscuro)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DetalleEquipoPreview() {
    ElectraTheme {
        DetalleEquipo(codigoEquipo = "TR-001")
    }
}
