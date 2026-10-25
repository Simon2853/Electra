package com.example.electra.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.Equipo
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.components.CampoValidado
import com.example.electra.ui.components.SelectorCampo
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.AzulSuave
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.TextoSecundario

private const val TODOS = "Todos"

@Composable
fun Equipos(
    onEquipoClick: (String) -> Unit = {},
    onVolverClick: (() -> Unit)? = null
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var tipoSeleccionado by rememberSaveable { mutableStateOf(TODOS) }

    val equiposFiltrados = CatalogoEquipos.filtrar(busqueda, tipoSeleccionado.takeIf { it != TODOS })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(titulo = "Equipos", subtitulo = "Selecciona el equipo a revisar", onVolver = onVolverClick)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                CampoValidado(
                    valor = busqueda,
                    onCambio = { busqueda = it },
                    etiqueta = "Buscar equipo",
                    icono = Icons.Outlined.Search,
                    error = null,
                    placeholder = "Código, cliente o instalación"
                )
            }

            item {
                SelectorCampo(
                    valor = tipoSeleccionado,
                    opciones = listOf(TODOS) + CatalogoEquipos.TIPOS,
                    onSeleccion = { tipoSeleccionado = it },
                    etiqueta = "Filtrar por tipo",
                    icono = Icons.Outlined.FilterList
                )
            }

            item {
                Text(
                    text = "${equiposFiltrados.size} equipos encontrados",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextoSecundario
                )
            }

            items(equiposFiltrados, key = { it.codigo }) { equipo ->
                EquipoCard(
                    equipo = equipo,
                    modifier = Modifier.animateItem(),
                    onClick = { onEquipoClick(equipo.codigo) }
                )
            }

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
                            tint = TextoSecundario,
                            modifier = Modifier.size(45.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No se encontraron equipos", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextoSecundario)
                        Text("Prueba con otro código o tipo.", fontSize = 13.sp, color = TextoSecundario)
                    }
                }
            }
        }
    }
}

@Composable
fun EquipoCard(
    equipo: Equipo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(AzulSuave, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Build, contentDescription = null, tint = AzulPrincipal, modifier = Modifier.size(26.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(equipo.codigo, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                Text(equipo.tipo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AzulPrincipal)
                Spacer(Modifier.height(4.dp))
                Text(equipo.cliente, fontSize = 13.sp, color = AzulOscuro)
                Text(equipo.instalacion, fontSize = 12.sp, color = TextoSecundario)
            }

            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = TextoSecundario)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EquiposPreview() {
    ElectraTheme {
        Equipos()
    }
}
