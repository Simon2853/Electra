package com.example.electra.ui.screen

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.device.ArchivosEvidencia
import com.example.electra.device.ResultadoUbicacion
import com.example.electra.device.Vibracion
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.OpcionesPrueba
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.components.BotonCarga
import com.example.electra.ui.components.CampoValidado
import com.example.electra.ui.components.MensajeError
import com.example.electra.ui.components.SelectorCampo
import com.example.electra.ui.components.TituloSeccion
import com.example.electra.ui.components.sacudir
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.AzulSuave
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.RojoError
import com.example.electra.ui.theme.RojoSuave
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.theme.VerdeExito
import com.example.electra.ui.theme.VerdeSuave
import com.example.electra.ui.viewmodel.EstadoUbicacion
import com.example.electra.ui.viewmodel.RegistrarPruebaUiState
import com.example.electra.ui.viewmodel.RegistrarPruebaViewModel
import com.example.electra.validation.Validaciones
import java.util.Calendar
import java.util.Locale

@Composable
fun RegistrarPrueba(
    viewModel: RegistrarPruebaViewModel,
    onVolverClick: () -> Unit = {},
    onPruebaGuardada: (Long) -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current


    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var camaraDenegada by rememberSaveable { mutableStateOf(false) }

    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = uriPendiente
        if (ok && uri != null) {
            estado.fotoUri?.let { ArchivosEvidencia.eliminar(context, it) }
            viewModel.onFotoTomada(uri)
        } else if (uri != null) {
            ArchivosEvidencia.eliminar(context, uri)
        }
        uriPendiente = null
    }

    fun abrirCamara() {
        val uri = ArchivosEvidencia.nuevaFoto(context)
        uriPendiente = uri.toString()
        tomarFoto.launch(uri)
    }

    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        camaraDenegada = !concedido
        if (concedido) abrirCamara()
    }


    val permisoUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos.values.any { it }) viewModel.capturarUbicacion() else viewModel.onPermisoUbicacionDenegado()
    }

    fun abrirAjustes() {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        )
    }


    LaunchedEffect(estado.intentosFallidos) {
        if (estado.intentosFallidos > 0) Vibracion.error(context)
    }

    LaunchedEffect(estado.idGuardado) {
        val id = estado.idGuardado ?: return@LaunchedEffect
        Vibracion.exito(context)
        viewModel.guardadoConsumido()
        onPruebaGuardada(id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(
            titulo = "Registrar prueba",
            subtitulo = "Registro de prueba en terreno",
            onVolver = onVolverClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Tarjeta(modifier = Modifier.sacudir(estado.intentosFallidos)) {
                TituloSeccion("Datos de la prueba")

                SelectorCampo(
                    valor = estado.equipo,
                    opciones = CatalogoEquipos.CODIGOS,
                    onSeleccion = viewModel::onEquipoChange,
                    etiqueta = "Equipo",
                    icono = Icons.Outlined.PrecisionManufacturing,
                    error = estado.errorEquipo
                )

                CatalogoEquipos.buscar(estado.equipo)?.let { equipo ->
                    Text(
                        text = "${equipo.tipo} · ${equipo.instalacion} · ${equipo.cliente}",
                        fontSize = 12.sp,
                        color = TextoSecundario,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                SelectorCampo(
                    valor = estado.tipoPrueba,
                    opciones = OpcionesPrueba.TIPOS,
                    onSeleccion = viewModel::onTipoPruebaChange,
                    etiqueta = "Tipo de prueba",
                    icono = Icons.AutoMirrored.Outlined.Assignment
                )

                SelectorFechaHora(estado, viewModel)
            }


            Tarjeta {
                TituloSeccion("Resultados")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoValidado(
                        valor = estado.medicion,
                        onCambio = viewModel::onMedicionChange,
                        etiqueta = "Medición",
                        icono = Icons.Outlined.Speed,
                        error = estado.errorMedicion,
                        valido = estado.medicionValida,
                        placeholder = "Ej: 125.5",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.4f)
                    )
                    SelectorCampo(
                        valor = estado.unidad,
                        opciones = OpcionesPrueba.UNIDADES,
                        onSeleccion = viewModel::onUnidadChange,
                        etiqueta = "Unidad",
                        icono = Icons.Outlined.Straighten,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Resultado", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AzulOscuro)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OpcionResultado(
                        texto = OpcionesPrueba.APROBADO,
                        icono = Icons.Filled.CheckCircle,
                        seleccionado = !estado.rechazada,
                        color = VerdeExito,
                        fondo = VerdeSuave,
                        onClick = { viewModel.onResultadoChange(OpcionesPrueba.APROBADO) },
                        modifier = Modifier.weight(1f)
                    )
                    OpcionResultado(
                        texto = OpcionesPrueba.RECHAZADO,
                        icono = Icons.Filled.Cancel,
                        seleccionado = estado.rechazada,
                        color = RojoError,
                        fondo = RojoSuave,
                        onClick = { viewModel.onResultadoChange(OpcionesPrueba.RECHAZADO) },
                        modifier = Modifier.weight(1f)
                    )
                }

                CampoValidado(
                    valor = estado.observaciones,
                    onCambio = viewModel::onObservacionesChange,
                    etiqueta = if (estado.rechazada) "Observaciones (obligatorio)" else "Observaciones",
                    icono = Icons.AutoMirrored.Outlined.Comment,
                    error = estado.errorObservaciones,
                    placeholder = if (estado.rechazada) "Motivo del rechazo..." else "Ingresa observaciones de la prueba...",
                    singleLine = false,
                    minLines = 3,
                    maxCaracteres = Validaciones.MAX_OBSERVACIONES
                )
            }


            Tarjeta {
                TituloSeccion("Evidencia en terreno")
                Text(
                    text = "Opcional: respalda la prueba con una foto del equipo y la ubicación GPS.",
                    fontSize = 12.sp,
                    color = TextoSecundario
                )

                SeccionFoto(
                    fotoUri = estado.fotoUri,
                    camaraDenegada = camaraDenegada,
                    onTomarFoto = { permisoCamara.launch(Manifest.permission.CAMERA) },
                    onQuitarFoto = {
                        estado.fotoUri?.let { ArchivosEvidencia.eliminar(context, it) }
                        viewModel.quitarFoto()
                    },
                    onAbrirAjustes = ::abrirAjustes
                )

                SeccionUbicacion(
                    estado = estado,
                    onCapturar = {
                        permisoUbicacion.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    },
                    onAbrirAjustes = ::abrirAjustes
                )
            }

            AnimatedVisibility(visible = estado.errorAlGuardar) {
                Text(
                    text = "No se pudo guardar la prueba. Inténtalo nuevamente.",
                    color = RojoError,
                    fontSize = 13.sp
                )
            }

            BotonCarga(
                texto = "Guardar prueba",
                icono = Icons.Outlined.Save,
                cargando = estado.guardando,
                onClick = viewModel::guardar
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Tarjeta(
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            contenido()
        }
    }
}

@Composable
private fun SelectorFechaHora(estado: RegistrarPruebaUiState, viewModel: RegistrarPruebaViewModel) {
    val context = LocalContext.current
    val color = if (estado.errorFecha != null) RojoError else AzulPrincipal

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    val (dia, mes, anio) = estado.fecha.split("/").map { it.toInt() }
                    DatePickerDialog(
                        context,
                        { _, a, m, d ->
                            viewModel.onFechaChange(String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, a))
                        },
                        anio, mes - 1, dia
                    ).apply { datePicker.maxDate = Calendar.getInstance().timeInMillis }.show()
                },
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, color),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(estado.fecha, color = color)
            }
            OutlinedButton(
                onClick = {
                    val (hora, minuto) = estado.hora.split(":").map { it.toInt() }
                    TimePickerDialog(
                        context,
                        { _, h, m -> viewModel.onHoraChange(String.format(Locale.getDefault(), "%02d:%02d", h, m)) },
                        hora, minuto, true
                    ).show()
                },
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, color),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(estado.hora, color = color)
            }
        }
        MensajeError(estado.errorFecha)
    }
}

@Composable
private fun OpcionResultado(
    texto: String,
    icono: ImageVector,
    seleccionado: Boolean,
    color: Color,
    fondo: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorFondo by animateColorAsState(if (seleccionado) fondo else Color.White, label = "fondoResultado")
    val colorContenido by animateColorAsState(if (seleccionado) color else TextoSecundario, label = "contenidoResultado")

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (seleccionado) 2.dp else 1.dp, colorContenido),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = colorFondo)
    ) {
        Icon(icono, contentDescription = null, tint = colorContenido, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, color = colorContenido, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun SeccionFoto(
    fotoUri: String?,
    camaraDenegada: Boolean,
    onTomarFoto: () -> Unit,
    onQuitarFoto: () -> Unit,
    onAbrirAjustes: () -> Unit
) {
    AnimatedContent(
        targetState = fotoUri,
        transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.9f)) togetherWith fadeOut() },
        label = "foto"
    ) { uri ->
        if (uri != null) {
            Box {
                AsyncImage(
                    model = uri,
                    contentDescription = "Foto de evidencia",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                IconButton(
                    onClick = onQuitarFoto,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "Quitar foto", tint = Color.White)
                }
            }
        } else {
            OutlinedButton(
                onClick = onTomarFoto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Tomar foto del equipo")
            }
        }
    }

    AnimatedVisibility(visible = camaraDenegada && fotoUri == null) {
        AvisoPermiso(
            texto = "Sin permiso de cámara no se puede adjuntar la foto.",
            onAbrirAjustes = onAbrirAjustes
        )
    }
}

@Composable
private fun SeccionUbicacion(
    estado: RegistrarPruebaUiState,
    onCapturar: () -> Unit,
    onAbrirAjustes: () -> Unit
) {
    val coordenadas = estado.coordenadas
    val capturada = estado.estadoUbicacion == EstadoUbicacion.CAPTURADA && coordenadas != null
    val colorFondo by animateColorAsState(if (capturada) VerdeSuave else AzulSuave, label = "fondoUbicacion")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorFondo, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(targetState = estado.estadoUbicacion, label = "iconoUbicacion") { ubicacion ->
            when (ubicacion) {
                EstadoUbicacion.BUSCANDO -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                EstadoUbicacion.CAPTURADA -> Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = VerdeExito)
                EstadoUbicacion.SIN_CAPTURAR -> Icon(Icons.Outlined.MyLocation, contentDescription = null, tint = AzulPrincipal)
                else -> Icon(Icons.Outlined.LocationOff, contentDescription = null, tint = RojoError)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Ubicación GPS", fontWeight = FontWeight.SemiBold, color = AzulOscuro, fontSize = 14.sp)
            Text(
                text = when (estado.estadoUbicacion) {
                    EstadoUbicacion.SIN_CAPTURAR -> "Registra dónde se realizó la prueba."
                    EstadoUbicacion.BUSCANDO -> "Buscando señal..."
                    EstadoUbicacion.CAPTURADA -> coordenadas?.let {
                        String.format(Locale.US, "%.5f, %.5f", it.latitud, it.longitud)
                    }.orEmpty()
                    EstadoUbicacion.SIN_PERMISO -> "Permiso de ubicación denegado."
                    EstadoUbicacion.GPS_DESACTIVADO -> "Activa el GPS del dispositivo."
                    EstadoUbicacion.NO_DISPONIBLE -> "Sin señal. Inténtalo al aire libre."
                },
                fontSize = 12.sp,
                color = TextoSecundario
            )
        }
        if (estado.estadoUbicacion == EstadoUbicacion.SIN_PERMISO) {
            TextButton(onClick = onAbrirAjustes) { Text("Ajustes") }
        } else {
            TextButton(onClick = onCapturar, enabled = estado.estadoUbicacion != EstadoUbicacion.BUSCANDO) {
                Text(if (capturada) "Actualizar" else "Capturar")
            }
        }
    }
}

@Composable
private fun AvisoPermiso(texto: String, onAbrirAjustes: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RojoSuave, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(texto, color = RojoError, fontSize = 12.sp, modifier = Modifier.weight(1f))
        TextButton(onClick = onAbrirAjustes) { Text("Ajustes") }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegistrarPruebaPreview() {
    ElectraTheme {
        RegistrarPrueba(
            viewModel = viewModel { RegistrarPruebaViewModel(
                PruebasRepositoryEnMemoria(),
                { ResultadoUbicacion.NoDisponible },
                "TR-001"
            ) }
        )
    }
}
