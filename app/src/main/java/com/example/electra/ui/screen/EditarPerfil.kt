package com.example.electra.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.electra.ui.components.Avatares
import com.example.electra.ui.components.AvatarIcono
import com.example.electra.ui.components.BarraSuperior
import com.example.electra.ui.components.SelectorColor
import com.example.electra.model.Avatar
import com.example.electra.ui.components.SelectorIconoAvatar
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
                        AvatarIcono(avatar = estado.avatar)
                        Text(
                            text = estado.nombre.ifBlank { "Tu nombre" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Avatares.contraste(fondo),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Text(text = "Ícono", fontSize = 13.sp, color = TextoSecundario)
                    SelectorIconoAvatar(
                        seleccionado = estado.avatar.icono,
                        color = Avatares.color(estado.avatar.color),
                        onSeleccionar = viewModel::onIconoChange
                    )

                    SelectorColor(
                        titulo = "Color del avatar",
                        seleccionado = Avatares.colorPaleta(estado.avatar.color),
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
