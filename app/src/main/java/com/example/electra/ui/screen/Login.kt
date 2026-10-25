package com.example.electra.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.R
import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.ui.components.BotonCarga
import com.example.electra.ui.components.CampoValidado
import com.example.electra.ui.components.sacudir
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.RojoError
import com.example.electra.ui.theme.RojoSuave
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.viewmodel.LoginViewModel

@Composable
fun Login(
    viewModel: LoginViewModel,
    onLoginClick: () -> Unit,
    onRegistroClick: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var contrasenaVisible by rememberSaveable { mutableStateOf(false) }


    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(estado.ingresoExitoso) {
        if (estado.ingresoExitoso) {
            viewModel.ingresoConsumido()
            onLoginClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -it / 3 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo Electra",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(8.dp))
                Text(text = "ELECTRA", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                Text(
                    text = "Gestión de pruebas eléctricas",
                    fontSize = 15.sp,
                    color = TextoSecundario,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(tween(600, delayMillis = 200)) { it / 3 }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .sacudir(estado.intentosFallidos),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text(text = "Iniciar sesión", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                        Text(
                            text = "Ingresa tus datos para continuar",
                            fontSize = 13.sp,
                            color = TextoSecundario,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )
                    }

                    CampoValidado(
                        valor = estado.usuario,
                        onCambio = viewModel::onUsuarioChange,
                        etiqueta = "Usuario",
                        icono = Icons.Outlined.Person,
                        error = estado.errorUsuario,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    CampoValidado(
                        valor = estado.contrasena,
                        onCambio = viewModel::onContrasenaChange,
                        etiqueta = "Contraseña",
                        icono = Icons.Outlined.Lock,
                        error = estado.errorContrasena,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        accionExtra = {
                            IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                                Icon(
                                    imageVector = if (contrasenaVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (contrasenaVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        }
                    )

                    AnimatedVisibility(visible = estado.credencialesInvalidas) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(RojoSuave, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = RojoError)
                            Spacer(Modifier.width(8.dp))
                            Text(text = "Usuario o contraseña incorrectos.", color = RojoError, fontSize = 13.sp)
                        }
                    }

                    BotonCarga(
                        texto = "Iniciar sesión",
                        icono = Icons.AutoMirrored.Outlined.Login,
                        cargando = estado.verificando,
                        onClick = viewModel::iniciarSesion
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onRegistroClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "¿No tienes cuenta? Regístrate", color = AzulPrincipal, fontSize = 14.sp)
        }

        Text(text = "Induztek Ingeniería SpA", fontSize = 12.sp, color = TextoSecundario, textAlign = TextAlign.Center)
        Text(
            text = "Registro y gestión de pruebas en terreno",
            fontSize = 11.sp,
            color = TextoSecundario,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginPreview() {
    ElectraTheme {
        Login(
            viewModel = viewModel { LoginViewModel(UsuariosRepositoryEnMemoria(), SesionEnMemoria()) },
            onLoginClick = {}
        )
    }
}
