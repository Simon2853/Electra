package com.example.electra.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.ManageAccounts
import com.example.electra.ui.components.EtiquetaRol
import com.example.electra.ui.components.icono
import com.example.electra.ui.theme.VerdeExito
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.electra.ui.components.Avatares
import com.example.electra.ui.components.AvatarUsuario
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.components.BotonCarga
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.RojoError
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.viewmodel.PerfilViewModel

@Composable
fun Perfil(
    viewModel: PerfilViewModel,
    onBackClick: () -> Unit = {},
    onSesionCerrada: () -> Unit = {},
    onEditarPerfilClick: () -> Unit = {},
    onCambiarContrasenaClick: () -> Unit = {},
    onUsuariosClick: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargar()
    }

    LaunchedEffect(estado.sesionCerrada) {
        if (estado.sesionCerrada) onSesionCerrada()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(titulo = "Perfil", subtitulo = "Cuenta y datos del dispositivo", onVolver = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val fondo = Avatares.fondo(estado.avatar.fondo)
            Tarjeta(colorFondo = fondo) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarUsuario(avatar = estado.avatar, nombre = estado.nombre, tamano = 64.dp, borde = 3.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(estado.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Avatares.contraste(fondo))
                        if (estado.usuario.isNotBlank()) {
                            Text(
                                text = "@${estado.usuario}",
                                fontSize = 14.sp,
                                color = Avatares.contrasteSecundario(fondo)
                            )
                        }
                        estado.rol?.let { EtiquetaRol(it) }
                    }
                }
            }

            estado.rol?.let { rol ->
                Tarjeta {
                    Text("Rol y permisos", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    FilaPerfil(rol.icono, "Rol", rol.nombre)
                    Text(rol.descripcion, fontSize = 13.sp, color = TextoSecundario)
                    HorizontalDivider()
                    rol.permisos.forEach { (permiso, permitido) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (permitido) Icons.Filled.CheckCircle else Icons.Outlined.Block,
                                contentDescription = if (permitido) "Permitido" else "No permitido",
                                tint = if (permitido) VerdeExito else TextoSecundario,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = permiso,
                                fontSize = 14.sp,
                                color = if (permitido) AzulOscuro else TextoSecundario
                            )
                        }
                    }
                    if (!rol.puedeGestionarUsuarios) {
                        Text(
                            text = "Para cambiar tu rol, pídeselo a un administrador.",
                            fontSize = 12.sp,
                            color = TextoSecundario
                        )
                    }
                }
            }

            Tarjeta {
                Text("Información", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                FilaPerfil(Icons.Outlined.Business, "Empresa", "Induztek Ingeniería SpA")
                HorizontalDivider()
                FilaPerfil(Icons.AutoMirrored.Outlined.Assignment, "Pruebas guardadas", estado.pruebasGuardadas.toString())
                HorizontalDivider()
                FilaPerfil(Icons.Outlined.PendingActions, "Protocolos por formalizar", estado.protocolosPendientes.toString())
                HorizontalDivider()
                FilaPerfil(Icons.Outlined.PhoneAndroid, "Almacenamiento", "Local en el dispositivo (funciona sin conexión)")
            }

            Tarjeta {
                Text("Cuenta", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                AccionPerfil(Icons.Outlined.Edit, "Editar perfil", "Nombre, foto, color y fondo", onEditarPerfilClick)
                HorizontalDivider()
                AccionPerfil(Icons.Outlined.LockReset, "Cambiar contraseña", "Requiere tu contraseña actual", onCambiarContrasenaClick)
            }

            if (estado.rol?.puedeGestionarUsuarios == true) {
                Tarjeta {
                    Text("Administración", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    AccionPerfil(Icons.Outlined.ManageAccounts, "Usuarios y roles", "Ver cuentas y cambiar sus roles", onUsuariosClick)
                }
            }

            BotonCarga(
                texto = "Cerrar sesión",
                icono = Icons.AutoMirrored.Outlined.Logout,
                cargando = false,
                onClick = { confirmarSalida = true },
                color = RojoError
            )

            Text(
                text = "ELECTRA • Induztek Ingeniería SpA",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = TextoSecundario
            )
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold, color = AzulOscuro) },
            text = { Text("Tus pruebas y protocolos seguirán guardados en el dispositivo.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    viewModel.cerrarSesion()
                }) { Text("Cerrar sesión", color = RojoError, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar", color = TextoSecundario) }
            }
        )
    }
}

@Composable
private fun Tarjeta(colorFondo: Color = Color.White, contenido: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            contenido()
        }
    }
}

@Composable
private fun AccionPerfil(icono: ImageVector, titulo: String, detalle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = AzulPrincipal, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AzulOscuro)
            Text(detalle, fontSize = 12.sp, color = TextoSecundario)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = TextoSecundario)
    }
}

@Composable
private fun FilaPerfil(icono: ImageVector, etiqueta: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = AzulPrincipal, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(etiqueta, fontSize = 12.sp, color = TextoSecundario)
            Text(valor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AzulOscuro)
        }
    }
}
