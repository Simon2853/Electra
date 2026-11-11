package com.example.electra.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.model.Rol
import com.example.electra.model.UsuarioResumen
import com.example.electra.ui.components.AccesoRestringido
import com.example.electra.ui.components.AvatarUsuario
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.components.EtiquetaRol
import com.example.electra.ui.components.SelectorRol
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.viewmodel.MensajeUsuarios
import com.example.electra.ui.viewmodel.UsuariosViewModel

@Composable
fun Usuarios(
    viewModel: UsuariosViewModel,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargar()
    }

    LaunchedEffect(estado.mensaje) {
        val texto = when (estado.mensaje) {
            MensajeUsuarios.ROL_ACTUALIZADO -> "Rol actualizado"
            MensajeUsuarios.PROPIO_ROL -> "No puedes cambiar tu propio rol; pídeselo a otro administrador."
            MensajeUsuarios.SIN_PERMISO -> "Solo un administrador puede cambiar roles"
            MensajeUsuarios.ERROR -> "No se pudo actualizar el rol"
            null -> return@LaunchedEffect
        }
        Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()
        viewModel.mensajeMostrado()
    }

    val rol = estado.rolActual
    if (rol != null && !rol.puedeGestionarUsuarios) {
        AccesoRestringido(rol = rol, accion = "gestionar usuarios", onVolver = onBackClick)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(titulo = "Usuarios y roles", subtitulo = "Administración", onVolver = onBackClick)

        if (estado.cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    estado.conteoPorRol.forEach { (rol, cantidad) ->
                        ResumenRol(rol = rol, cantidad = cantidad, modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Text(
                    text = "Toca una cuenta para cambiar su rol.",
                    fontSize = 13.sp,
                    color = TextoSecundario
                )
            }

            items(estado.usuarios, key = { it.usuario }) { usuario ->
                FilaUsuario(
                    usuario = usuario,
                    esTuCuenta = usuario.usuario == estado.usuarioActual,
                    onClick = { viewModel.abrirCambioRol(usuario) }
                )
            }
        }
    }

    estado.editando?.let { usuario ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarCambioRol,
            title = { Text("Rol de ${usuario.nombre}", fontWeight = FontWeight.Bold, color = AzulOscuro) },
            text = { SelectorRol(seleccionado = estado.rolElegido, onSeleccionar = viewModel::onRolElegido) },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmarCambioRol,
                    enabled = estado.rolElegido != usuario.rol
                ) { Text("Guardar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarCambioRol) { Text("Cancelar", color = TextoSecundario) }
            }
        )
    }
}

@Composable
private fun ResumenRol(rol: Rol, cantidad: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(cantidad.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
            Text(rol.nombre, fontSize = 11.sp, color = TextoSecundario, maxLines = 2)
        }
    }
}

@Composable
private fun FilaUsuario(usuario: UsuarioResumen, esTuCuenta: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarUsuario(avatar = usuario.avatar, nombre = usuario.nombre, tamano = 44.dp, borde = 2.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (esTuCuenta) "${usuario.nombre} (tú)" else usuario.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulOscuro
                )
                Text("@${usuario.usuario}", fontSize = 12.sp, color = TextoSecundario)
                EtiquetaRol(usuario.rol)
            }
            if (!esTuCuenta) {
                Text("Cambiar", fontSize = 13.sp, color = AzulPrincipal, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UsuariosPreview() {
    ElectraTheme {
        Usuarios(viewModel = viewModel { UsuariosViewModel(UsuariosRepositoryEnMemoria(), SesionEnMemoria()) })
    }
}
