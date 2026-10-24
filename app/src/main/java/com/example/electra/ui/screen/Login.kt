package com.example.electra.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.electra.R
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.ui.viewmodel.LoginViewModel

@Composable
fun Login(
    viewModel: LoginViewModel,
    onLoginClick: () -> Unit,
    onRegistroClick: () -> Unit = {}
) {

    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val errorLogin = estado.error

    LaunchedEffect(estado.ingresoExitoso) {
        if (estado.ingresoExitoso) {
            viewModel.ingresoConsumido()
            onLoginClick()
        }
    }

    var usuario by remember {
        mutableStateOf("")
    }

    var contraseña by remember {
        mutableStateOf("")
    }

    var contraseñaVisible by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // =====================================================
        // LOGO
        // =====================================================

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo Electra",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentScale = ContentScale.Fit
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        // =====================================================
        // NOMBRE DE LA APLICACIÓN
        // =====================================================

        Text(
            text = "ELECTRA",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF173B57)
        )

        Text(
            text = "Gestión de pruebas eléctricas",
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // =====================================================
        // TARJETA DE LOGIN
        // =====================================================

        Card(

            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),

            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                // =================================================
                // TÍTULO
                // =================================================

                Text(
                    text = "Iniciar sesión",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF173B57)
                )

                Text(
                    text = "Ingresa tus datos para continuar",
                    fontSize = 13.sp,
                    color = Color.Gray,

                    modifier = Modifier.padding(
                        top = 4.dp,
                        bottom = 20.dp
                    )
                )


                // =================================================
                // USUARIO
                // =================================================

                OutlinedTextField(

                    value = usuario,
                    onValueChange = {
                        usuario = it
                    },

                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text("Usuario")
                    },

                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Usuario"
                        )
                    },

                    shape = RoundedCornerShape(10.dp)
                )


                Spacer(
                    modifier = Modifier.height(12.dp)
                )


                // =================================================
                // CONTRASEÑA
                // =================================================

                OutlinedTextField(

                    value = contraseña,
                    onValueChange = {
                        contraseña = it
                    },

                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text("Contraseña")
                    },

                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Contraseña"
                        )
                    },

                    trailingIcon = {

                        IconButton(
                            onClick = {
                                contraseñaVisible =
                                    !contraseñaVisible
                            }
                        ) {

                            Icon(

                                imageVector =
                                    if (contraseñaVisible)
                                        Icons.Outlined.VisibilityOff
                                    else
                                        Icons.Outlined.Visibility,

                                contentDescription =
                                    if (contraseñaVisible)
                                        "Ocultar contraseña"
                                    else
                                        "Mostrar contraseña"
                            )
                        }
                    },

                    visualTransformation =
                        if (contraseñaVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),

                    shape = RoundedCornerShape(10.dp)
                )


                Spacer(
                    modifier = Modifier.height(20.dp)
                )


                // =================================================
                // BOTÓN INICIAR SESIÓN
                // =================================================


                Button(
                    onClick = {
                        viewModel.iniciarSesion(usuario, contraseña)
                    },
                    enabled = !estado.verificando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1769AA),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Iniciar sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (errorLogin) {
                    Text(
                        text = "Usuario o contraseña incorrectos.",
                        color = Color(0xFFC62828),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        TextButton(
            onClick = onRegistroClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "¿No tienes cuenta? Regístrate",
                color = Color(0xFF1769AA),
                fontSize = 14.sp
            )
        }


        // =====================================================
        // INFORMACIÓN
        // =====================================================

        Text(
            text = "Induztek Ingeniería SpA",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Registro y gestión de pruebas en terreno",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}


// =========================================================
// PREVIEW
// =========================================================

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun LoginPreview() {
    Login(
        viewModel = LoginViewModel(UsuariosRepositoryEnMemoria()),
        onLoginClick = {},
        onRegistroClick = {}
    )
}