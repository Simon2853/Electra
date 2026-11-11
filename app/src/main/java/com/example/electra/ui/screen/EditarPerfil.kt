package com.example.electra.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.device.ArchivosEvidencia
import com.example.electra.ui.components.Avatares
import com.example.electra.ui.components.AvatarUsuario
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.components.SelectorColor
import com.example.electra.model.Avatar
import com.example.electra.ui.components.SelectorColorAvatar
import com.example.electra.ui.theme.RojoError
import com.example.electra.ui.components.BotonCarga
import com.example.electra.ui.components.CampoValidado
import com.example.electra.ui.components.TituloSeccion
import com.example.electra.ui.components.sacudir
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.viewmodel.EditarPerfilViewModel

@Composable
fun EditarPerfil(
    viewModel: EditarPerfilViewModel,
    onVolverClick: () -> Unit = {},
    onPerfilGuardado: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Misma mecánica que la foto de evidencia en RegistrarPrueba: TakePicture + FileProvider.
    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = uriPendiente
        if (ok && uri != null) viewModel.onFotoElegida(uri)
        else if (uri != null) ArchivosEvidencia.eliminar(context, uri)
        uriPendiente = null
    }

    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            val uri = ArchivosEvidencia.nuevaFotoPerfil(context)
            uriPendiente = uri.toString()
            tomarFoto.launch(uri)
        } else {
            viewModel.onErrorFoto("Sin permiso de cámara. Puedes elegir una foto de la galería.")
        }
    }

    // El selector de fotos del sistema no necesita permisos de almacenamiento.
    val elegirGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val copia = ArchivosEvidencia.copiarFotoPerfil(context, uri)
        if (copia != null) viewModel.onFotoElegida(copia.toString())
        else viewModel.onErrorFoto("No se pudo leer la imagen seleccionada.")
    }

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            viewModel.guardadoConsumido()
            onPerfilGuardado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(titulo = "Editar perfil", subtitulo = "Datos personales", onVolver = onVolverClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val fondo = Avatares.fondo(estado.avatar.fondo)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TituloSeccion("Apariencia")

                    // Vista previa de la cabecera del perfil con el fondo elegido
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(fondo, RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFDDE3EA), RoundedCornerShape(14.dp))
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AvatarUsuario(avatar = estado.avatar, nombre = estado.nombre.ifBlank { estado.usuario })
                        Text(
                            text = estado.nombre.ifBlank { "Tu nombre" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Avatares.contraste(fondo),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Text(text = "Foto de perfil", fontSize = 13.sp, color = TextoSecundario)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { permisoCamara.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Tomar foto")
                        }
                        OutlinedButton(
                            onClick = {
                                elegirGaleria.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Galería")
                        }
                    }
                    if (estado.avatar.foto != null) {
                        TextButton(onClick = viewModel::onQuitarFoto) {
                            Icon(Icons.Outlined.Delete, contentDescription = null, tint = RojoError, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Quitar foto y usar iniciales", color = RojoError)
                        }
                    }
                    estado.errorFoto?.let { Text(it, fontSize = 12.sp, color = RojoError) }

                    Text(
                        text = if (estado.avatar.foto != null) "Color de las iniciales (si quitas la foto)" else "Color de las iniciales",
                        fontSize = 13.sp,
                        color = TextoSecundario
                    )
                    SelectorColorAvatar(
                        seleccionado = estado.avatar.color,
                        onSeleccionar = viewModel::onColorChange
                    )

                    SelectorColor(
                        titulo = "Fondo del perfil",
                        seleccionado = Avatares.colorPaleta(estado.avatar.fondo, Avatar.FONDO_POR_DEFECTO),
                        onSeleccionar = viewModel::onFondoChange
                    )
                }
            }

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
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )

                    CampoValidado(
                        valor = estado.usuario,
                        onCambio = {},
                        etiqueta = "Nombre de usuario",
                        icono = Icons.Outlined.AccountCircle,
                        error = null,
                        readOnly = true
                    )

                    Text(
                        text = "El nombre de usuario identifica tu cuenta y no se puede cambiar.",
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )
                }
            }

            BotonCarga(
                texto = "Guardar cambios",
                icono = Icons.Outlined.Save,
                cargando = estado.guardando,
                onClick = viewModel::guardar
            )

            TextButton(onClick = onVolverClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Cancelar", color = AzulPrincipal)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditarPerfilPreview() {
    ElectraTheme {
        EditarPerfil(viewModel = viewModel { EditarPerfilViewModel(UsuariosRepositoryEnMemoria(), SesionEnMemoria()) })
    }
}
