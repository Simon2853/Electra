package com.example.electra.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.components.BotonCarga
import com.example.electra.ui.components.CampoValidado
import com.example.electra.ui.components.TituloSeccion
import com.example.electra.ui.components.sacudir
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.NaranjaPendiente
import com.example.electra.ui.theme.RojoError
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.theme.VerdeExito
import com.example.electra.ui.viewmodel.RegistroViewModel

@Composable
fun Registro(
    viewModel: RegistroViewModel,
    onVolverClick: () -> Unit = {},
    onRegistroExitoso: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var contrasenaVisible by rememberSaveable { mutableStateOf(false) }
    var confirmarVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(estado.registroExitoso) {
        if (estado.registroExitoso) {
            viewModel.registroConsumido()
            onRegistroExitoso()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(titulo = "Crear cuenta", subtitulo = "Registro de técnico", onVolver = onVolverClick)

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
                    TituloSeccion("Datos personales")

                    CampoValidado(
                        valor = estado.nombre,
                        onCambio = viewModel::onNombreChange,
                        etiqueta = "Nombre completo",
                        icono = Icons.Outlined.Badge,
                        error = estado.errorNombre,
                        valido = estado.nombreValido,
                        maxCaracteres = 50,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    CampoValidado(
                        valor = estado.usuario,
                        onCambio = viewModel::onUsuarioChange,
                        etiqueta = "Nombre de usuario",
                        icono = Icons.Outlined.AccountCircle,
                        error = estado.errorUsuario,
                        valido = estado.usuarioValido,
                        placeholder = "Ej: jperez",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    TituloSeccion("Seguridad", modifier = Modifier.padding(top = 4.dp))

                    CampoValidado(
                        valor = estado.contrasena,
                        onCambio = viewModel::onContrasenaChange,
                        etiqueta = "Contraseña",
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
                texto = "Crear cuenta",
                icono = Icons.Outlined.PersonAdd,
                cargando = estado.registrando,
                onClick = viewModel::registrar
            )

            TextButton(onClick = onVolverClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Volver al inicio de sesión", color = AzulPrincipal)
            }
        }
    }
}

@Composable
private fun BotonVisibilidad(visible: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
        )
    }
}

@Composable
private fun IndicadorFortaleza(requisitos: List<Pair<String, Boolean>>) {
    val cumplidos = requisitos.count { it.second }
    val progreso by animateFloatAsState(targetValue = cumplidos / requisitos.size.toFloat(), label = "fortaleza")
    val color by animateColorAsState(
        targetValue = when {
            cumplidos == requisitos.size -> VerdeExito
            cumplidos >= 3 -> NaranjaPendiente
            else -> RojoError
        },
        label = "colorFortaleza"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = Fondo
        )
        requisitos.forEach { (texto, cumplido) ->
            val colorRequisito by animateColorAsState(
                targetValue = if (cumplido) VerdeExito else TextoSecundario,
                label = "requisito"
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (cumplido) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = if (cumplido) "Cumplido" else "Pendiente",
                    tint = colorRequisito,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(text = texto, fontSize = 12.sp, color = colorRequisito)
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegistroPreview() {
    ElectraTheme {
        Registro(viewModel = viewModel { RegistroViewModel(UsuariosRepositoryEnMemoria()) })
    }
}
