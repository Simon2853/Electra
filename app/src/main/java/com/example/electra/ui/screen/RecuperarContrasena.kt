package com.example.electra.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.components.BotonCarga
import com.example.electra.ui.components.CampoValidado
import com.example.electra.ui.components.TituloSeccion
import com.example.electra.ui.components.sacudir
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.RojoError
import com.example.electra.ui.theme.RojoSuave
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.viewmodel.RecuperarContrasenaViewModel

@Composable
fun RecuperarContrasena(
    viewModel: RecuperarContrasenaViewModel,
    onVolverClick: () -> Unit = {},
    onContrasenaRestablecida: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var contrasenaVisible by rememberSaveable { mutableStateOf(false) }
    var confirmarVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(estado.restablecida) {
        if (estado.restablecida) {
            viewModel.restablecidaConsumida()
            onContrasenaRestablecida()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(titulo = "Recuperar contraseña", subtitulo = "Restablece tu acceso", onVolver = onVolverClick)

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
                    TituloSeccion("Verifica tu identidad")
                    Text(
                        text = "Ingresa tu usuario y el nombre completo con el que te registraste.",
                        fontSize = 13.sp,
                        color = TextoSecundario
                    )

                    CampoValidado(
                        valor = estado.usuario,
                        onCambio = viewModel::onUsuarioChange,
                        etiqueta = "Nombre de usuario",
                        icono = Icons.Outlined.AccountCircle,
                        error = estado.errorUsuario,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    CampoValidado(
                        valor = estado.nombre,
                        onCambio = viewModel::onNombreChange,
                        etiqueta = "Nombre completo",
                        icono = Icons.Outlined.Badge,
                        error = estado.errorNombre,
                        maxCaracteres = 50,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    AnimatedVisibility(visible = estado.datosNoCoinciden) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(RojoSuave, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = RojoError)
                            Spacer(Modifier.width(8.dp))
                            Text(text = "El usuario y el nombre no coinciden con ninguna cuenta.", color = RojoError, fontSize = 13.sp)
                        }
                    }

                    TituloSeccion("Nueva contraseña", modifier = Modifier.padding(top = 4.dp))

                    CampoValidado(
                        valor = estado.contrasena,
                        onCambio = viewModel::onContrasenaChange,
                        etiqueta = "Nueva contraseña",
                        icono = Icons.Outlined.Lock,
                        error = estado.errorContrasena,
                        valido = estado.contrasenaValida,
                        maxCaracteres = 20,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        accionExtra = {
                            BotonVisibilidad(contrasenaVisible) { contrasenaVisible = !contrasenaVisible }
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
                texto = "Restablecer contraseña",
                icono = Icons.Outlined.LockReset,
                cargando = estado.guardando,
                onClick = viewModel::restablecer
            )

            TextButton(onClick = onVolverClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Volver al inicio de sesión", color = AzulPrincipal)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RecuperarContrasenaPreview() {
    ElectraTheme {
        RecuperarContrasena(viewModel = viewModel { RecuperarContrasenaViewModel(UsuariosRepositoryEnMemoria()) })
    }
}
