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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.NaranjaPendiente
import com.example.electra.ui.theme.RojoError
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.theme.VerdeExito
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
import com.example.electra.model.Protocolo
import com.example.electra.ui.viewmodel.LIMITE_RECIENTES_POR_EQUIPO
import com.example.electra.ui.viewmodel.ProtocolosViewModel

private val BackgroundColor = Fondo
private val DarkBlue = AzulOscuro
private val MainBlue = AzulPrincipal
private val SuccessGreen = VerdeExito
private val Orange = NaranjaPendiente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Protocolos(
    viewModel: ProtocolosViewModel,
    onBackClick: () -> Unit = {},
    onProtocolClick: (String) -> Unit = {},
    onCrearProtocoloClick: () -> Unit = {}
) {
    val protocolosBd by viewModel.protocolos.collectAsStateWithLifecycle()

    var busquedaEquipo by remember { mutableStateOf("") }
    var fechaSeleccionada by remember { mutableStateOf("Todas") }
    var estadoSeleccionado by remember { mutableStateOf("Todos") }
    var fechaExpanded by remember { mutableStateOf(false) }
    var estadoExpanded by remember { mutableStateOf(false) }


    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargar()
    }

    val opcionesFechas = remember(protocolosBd) {
        listOf("Todas") + protocolosBd.map { it.fecha }.distinct()
    }

    val protocolosFiltrados = protocolosBd.filter { protocolo ->
        val coincideEquipo = protocolo.equipo.contains(busquedaEquipo, ignoreCase = true)
        val coincideFecha = fechaSeleccionada == "Todas" || protocolo.fecha == fechaSeleccionada
        val coincideEstado = estadoSeleccionado == "Todos" || protocolo.estado == estadoSeleccionado
        coincideEquipo && coincideFecha && coincideEstado
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCrearProtocoloClick,
                containerColor = MainBlue,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo Protocolo"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundColor)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                        text = "Lista de protocolos",
                        fontSize = 13.sp,
                        color = TextoSecundario
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {


                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MainBlue
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "PROTOCOLOS",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = DarkBlue
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Filtros",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBlue
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = busquedaEquipo,
                                onValueChange = { busquedaEquipo = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Equipo") },
                                placeholder = { Text("Ej: TRA-001") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            ExposedDropdownMenuBox(
                                expanded = fechaExpanded,
                                onExpandedChange = { fechaExpanded = !fechaExpanded }
                            ) {
                                OutlinedTextField(
                                    value = fechaSeleccionada,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                    label = { Text("Fecha") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = fechaExpanded)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )

                                ExposedDropdownMenu(
                                    expanded = fechaExpanded,
                                    onDismissRequest = { fechaExpanded = false }
                                ) {
                                    opcionesFechas.forEach { fecha ->
                                        DropdownMenuItem(
                                            text = { Text(fecha) },
                                            onClick = {
                                                fechaSeleccionada = fecha
                                                fechaExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            ExposedDropdownMenuBox(
                                expanded = estadoExpanded,
                                onExpandedChange = { estadoExpanded = !estadoExpanded }
                            ) {
                                OutlinedTextField(
                                    value = estadoSeleccionado,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                    label = { Text("Estado") },
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
                                    listOf("Todos", "Formalizado", "Registrado en terreno").forEach { estado ->
                                        DropdownMenuItem(
                                            text = { Text(estado) },
                                            onClick = {
                                                estadoSeleccionado = estado
                                                estadoExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = "${protocolosFiltrados.size} protocolo(s) encontrado(s)",
                            fontSize = 13.sp,
                            color = TextoSecundario
                        )

                        Text(
                            text = "Se muestran los últimos $LIMITE_RECIENTES_POR_EQUIPO de cada equipo",
                            fontSize = 12.sp,
                            color = TextoSecundario
                        )
                    }
                }

                items(protocolosFiltrados, key = { it.id }) { protocolo ->
                    ProtocoloListItem(
                        modifier = Modifier.animateItem(),
                        protocolo = protocolo,
                        onClick = {
                            val numeroLimpio = protocolo.numero.replace("#", "")
                            onProtocolClick(numeroLimpio)
                        }
                    )
                }

                if (protocolosFiltrados.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = TextoSecundario
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "No se encontraron protocolos",
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBlue
                                )

                                Text(
                                    text = "Prueba modificando los filtros.",
                                    fontSize = 13.sp,
                                    color = TextoSecundario
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ELECTRA • Induztek Ingeniería SpA",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtocoloListItem(
    protocolo: Protocolo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = protocolo.numero,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Equipo: ${protocolo.equipo}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MainBlue
                    )
                }

                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MainBlue
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = protocolo.fecha,
                    fontSize = 14.sp,
                    color = TextoSecundario
                )

                EstadoProtocolo(protocolo.estado)
            }
        }
    }
}

@Composable
private fun EstadoProtocolo(
    estado: String
) {
    val esFormalizado = estado == "Formalizado"
    val color = if (esFormalizado) SuccessGreen else Orange

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (esFormalizado) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = color
            )
        } else {
            Text(
                text = "●",
                fontSize = 18.sp,
                color = color
            )
        }

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = estado,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProtocolosPreview() {
    MaterialTheme {
        Protocolos(
            viewModel = viewModel { ProtocolosViewModel(
                ProtocolosRepositoryEnMemoria(
                    listOf(Protocolo(id = 1, numero = "#000125", equipo = "TR-001", fecha = "02/10/2026", estado = "Formalizado"))
                )
            ) }
        )
    }
}