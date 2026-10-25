package com.example.electra.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.data.ProtocolosRepositoryEnMemoria
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.device.Vibracion
import com.example.electra.model.EstadosProtocolo
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.components.BotonCarga
import com.example.electra.ui.components.CampoValidado
import com.example.electra.ui.components.SelectorCampo
import com.example.electra.ui.components.TituloSeccion
import com.example.electra.ui.components.sacudir
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.theme.VerdeExito
import com.example.electra.ui.viewmodel.CrearProtocoloViewModel
import com.example.electra.ui.viewmodel.ResultadoProtocolo
import com.example.electra.validation.Validaciones

@Composable
fun CrearProtocolo(
    viewModel: CrearProtocoloViewModel,
    onBackClick: () -> Unit = {},
    onProtocoloGuardado: () -> Unit = {}
) {
    val context = LocalContext.current
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(estado.resultado) {
        when (estado.resultado) {
            ResultadoProtocolo.CAMPOS_INCOMPLETOS, ResultadoProtocolo.DUPLICADO -> Vibracion.error(context)
            ResultadoProtocolo.EXITO -> {
                Vibracion.exito(context)
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
            .background(Fondo)
    ) {
        BarraSuperior(titulo = "Nuevo protocolo", subtitulo = "Formalización del trabajo en terreno", onVolver = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .sacudir(estado.intentosFallidos),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TituloSeccion("Datos del protocolo")

                    CampoValidado(
                        valor = estado.numero,
                        onCambio = viewModel::onNumeroChange,
                        etiqueta = "Número de protocolo",
                        icono = Icons.AutoMirrored.Outlined.Assignment,
                        error = estado.errorNumero,
                        valido = estado.numeroValido,
                        placeholder = "#000130",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )

                    SelectorCampo(
                        valor = estado.equipo,
                        opciones = estado.equipos,
                        onSeleccion = viewModel::onEquipoChange,
                        etiqueta = "Equipo asignado",
                        icono = Icons.Outlined.PrecisionManufacturing
                    )

                    CampoValidado(
                        valor = estado.fecha,
                        onCambio = {},
                        etiqueta = "Fecha de emisión",
                        icono = Icons.Outlined.CalendarToday,
                        error = null,
                        readOnly = true
                    )

                    SelectorCampo(
                        valor = estado.estado,
                        opciones = EstadosProtocolo.TODOS,
                        onSeleccion = viewModel::onEstadoChange,
                        etiqueta = "Estado del protocolo",
                        icono = Icons.Outlined.Flag
                    )

                    CampoValidado(
                        valor = estado.responsable,
                        onCambio = viewModel::onResponsableChange,
                        etiqueta = "Responsable / Inspector",
                        icono = Icons.Outlined.Person,
                        error = estado.errorResponsable,
                        valido = estado.responsableValido,
                        placeholder = "Ej: Juan Pérez",
                        maxCaracteres = 50
                    )

                    CampoValidado(
                        valor = estado.observaciones,
                        onCambio = viewModel::onObservacionesChange,
                        etiqueta = "Observaciones / Notas",
                        icono = Icons.AutoMirrored.Outlined.Comment,
                        error = estado.errorObservaciones,
                        placeholder = "Escribe las notas del protocolo aquí...",
                        singleLine = false,
                        minLines = 3,
                        maxCaracteres = Validaciones.MAX_OBSERVACIONES
                    )
                }
            }

            BotonCarga(
                texto = "Guardar protocolo",
                icono = Icons.Outlined.Save,
                cargando = estado.guardando,
                onClick = viewModel::guardar,
                color = VerdeExito
            )

            Text(
                text = "ELECTRA • Induztek Ingeniería SpA",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = TextoSecundario
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CrearProtocoloPreview() {
    ElectraTheme {
        CrearProtocolo(
            viewModel = viewModel { CrearProtocoloViewModel(ProtocolosRepositoryEnMemoria(), PruebasRepositoryEnMemoria()) }
        )
    }
}
