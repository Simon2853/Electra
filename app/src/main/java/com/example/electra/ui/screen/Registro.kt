package com.example.electra.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.ui.viewmodel.RegistroViewModel

@Composable
fun Registro(
    viewModel: RegistroViewModel,
    onVolverClick: () -> Unit = {},
    onRegistroExitoso: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val error = estado.error

    LaunchedEffect(estado.registroExitoso) {
        if (estado.registroExitoso) {
            viewModel.registroConsumido()
            onRegistroExitoso()
        }
    }

    var nombre by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var contrasenaVisible by remember { mutableStateOf(false) }
    var confirmarVisible by remember { mutableStateOf(false) }

    val azulOscuro = Color(0xFF173B57)
    val azul = Color(0xFF1769AA)



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FA))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = {
                    onVolverClick()
                },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Volver al inicio de sesión",
                    tint = Color(0xFF173B57),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Text(
            text = "ELECTRA",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = azulOscuro
        )

        Text(
            text = "Crear cuenta",
            fontSize = 22.sp,
            color = azulOscuro,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = usuario,
                    onValueChange = { usuario = it },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = {
                        if (it.length <= 20) contrasena = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (contrasenaVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                contrasenaVisible = !contrasenaVisible
                            }
                        ) {
                            Icon(
                                imageVector = if (contrasenaVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = if (contrasenaVisible) {
                                    "Ocultar contraseña"
                                } else {
                                    "Mostrar contraseña"
                                }
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                )


                Text(
                    text = "Requisitos de la contraseña",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = azulOscuro,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                val requisitos = listOf(
                    "Entre 8 y 20 caracteres" to
                            (contrasena.length in 8..20),

                    "Al menos una letra mayúscula" to
                            contrasena.any { it.isUpperCase() },

                    "Al menos una letra minúscula" to
                            contrasena.any { it.isLowerCase() },

                    "Al menos un número" to
                            contrasena.any { it.isDigit() },

                    "Al menos un carácter especial" to
                            contrasena.any { !it.isLetterOrDigit() },

                    "Caracteres especiales disponibles: ! @ # $ % & * ?" to true
                )

                requisitos.forEach { (texto, cumplido) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = if (cumplido) "✓" else "•",
                            color = if (cumplido) {
                                Color(0xFF2E7D32)
                            } else {
                                Color.Gray
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        Text(
                            text = texto,
                            fontSize = 12.sp,
                            color = if (cumplido) {
                                Color(0xFF2E7D32)
                            } else {
                                Color.Gray
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = confirmar,
                    onValueChange = {
                        if (it.length <= 20) confirmar = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    visualTransformation = if (confirmarVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                confirmarVisible = !confirmarVisible
                            }
                        ) {
                            Icon(
                                imageVector = if (confirmarVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = if (confirmarVisible) {
                                    "Ocultar contraseña"
                                } else {
                                    "Mostrar contraseña"
                                }
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                )

                if (error.isNotBlank()) {
                    Text(
                        text = error,
                        color = Color(0xFFC62828)
                    )
                }

                Button(
                    onClick = {
                        viewModel.registrar(nombre, usuario, contrasena, confirmar)
                    },
                    enabled = !estado.registrando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = azul
                    )
                ) {
                    Text("Crear cuenta")
                }

                TextButton(
                    onClick = onVolverClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Volver al inicio de sesión",
                        color = azul
                    )
                }
            }
        }

        Text(
            text = "Induztek Ingeniería SpA",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}
