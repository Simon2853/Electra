package com.example.electra.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.components.BotonCarga
import com.example.electra.ui.components.CampoValidado
import com.example.electra.ui.components.TituloSeccion
import com.example.electra.ui.components.sacudir
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.viewmodel.CambiarContrasenaViewModel

@Composable
fun CambiarContrasena(
    viewModel: CambiarContrasenaViewModel,
    onVolverClick: () -> Unit = {},
    onContrasenaCambiada: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var actualVisible by rememberSaveable { mutableStateOf(false) }
    var nuevaVisible by rememberSaveable { mutableStateOf(false) }
    var confirmarVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(estado.cambiada) {
        if (estado.cambiada) {
            viewModel.cambiadaConsumida()
            onContrasenaCambiada()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(titulo = "Cambiar contraseña", subtitulo = "Seguridad de la cuenta", onVolver = onVolverClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
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
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TituloSeccion("Contraseña actual")

                    CampoValidado(
                        valor = estado.actual,
                        onCambio = viewModel::onActualChange,
                        etiqueta = "Contraseña actual",
                        icono = Icons.Outlined.Key,
                        error = estado.errorActual,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        visualTransformation = if (actualVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        accionExtra = {
                            BotonVisibilidad(actualVisible) { actualVisible = !actualVisible }
                        }
                    )

                    TituloSeccion("Nueva contraseña", modifier = Modifier.padding(top = 4.dp))

                    CampoValidado(
                        valor = estado.nueva,
                        onCambio = viewModel::onNuevaChange,
                        etiqueta = "Nueva contraseña",
                        icono = Icons.Outlined.Lock,
                        error = estado.errorNueva,
                        valido = estado.nuevaValida,
                        maxCaracteres = 20,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        visualTransformation = if (nuevaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        accionExtra = {
                            BotonVisibilidad(nuevaVisible) { nuevaVisible = !nuevaVisible }
                        }
                    )

                    IndicadorFortaleza(estado.requisitos)

                    CampoValidado(
                        valor = estado.confirmar,
                        onCambio = viewModel::onConfirmarChange,
                        etiqueta = "Confirmar contraseña",
                        icono = Icons.Outlined.LockReset,
                        error = estado.errorConfirmar,
                        valido = estado.confirmarValido,
                        maxCaracteres = 20,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        visualTransformation = if (confirmarVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        accionExtra = {
                            BotonVisibilidad(confirmarVisible) { confirmarVisible = !confirmarVisible }
                        }
                    )
                }
            }

            BotonCarga(
                texto = "Cambiar contraseña",
                icono = Icons.Outlined.LockReset,
                cargando = estado.guardando,
                onClick = viewModel::cambiar
            )

            TextButton(onClick = onVolverClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Cancelar", color = AzulPrincipal)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CambiarContrasenaPreview() {
    ElectraTheme {
        CambiarContrasena(
            viewModel = viewModel { CambiarContrasenaViewModel(UsuariosRepositoryEnMemoria(), SesionEnMemoria()) }
        )
    }
}
