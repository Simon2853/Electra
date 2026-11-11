package com.example.electra.ui.components

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.electra.model.Rol
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.AzulSuave
import com.example.electra.ui.theme.ElectraTheme
import com.example.electra.ui.theme.Fondo
import com.example.electra.ui.theme.NaranjaPendiente
import com.example.electra.ui.theme.NaranjaSuave
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.theme.VerdeExito
import com.example.electra.ui.theme.VerdeSuave

/** Ícono y colores con que se identifica cada rol en toda la app. */
val Rol.icono: ImageVector
    get() = when (this) {
        Rol.TECNICO -> Icons.Outlined.Engineering
        Rol.SUPERVISOR -> Icons.Outlined.VerifiedUser
        Rol.ADMINISTRADOR -> Icons.Outlined.AdminPanelSettings
    }

private val Rol.colorTexto: Color
    get() = when (this) {
        Rol.TECNICO -> AzulPrincipal
        Rol.SUPERVISOR -> VerdeExito
        Rol.ADMINISTRADOR -> NaranjaPendiente
    }

private val Rol.colorFondo: Color
    get() = when (this) {
        Rol.TECNICO -> AzulSuave
        Rol.SUPERVISOR -> VerdeSuave
        Rol.ADMINISTRADOR -> NaranjaSuave
    }

/** Etiqueta compacta con el nombre del rol (perfil, lista de usuarios). */
@Composable
fun EtiquetaRol(rol: Rol, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(rol.colorFondo, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(rol.icono, contentDescription = null, tint = rol.colorTexto, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(rol.nombre, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = rol.colorTexto)
    }
}

/** Lista de roles para elegir uno (registro y gestión de usuarios). */
@Composable
fun SelectorRol(
    seleccionado: Rol,
    onSeleccionar: (Rol) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Rol.entries.forEach { rol ->
            val activo = rol == seleccionado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (activo) rol.colorFondo else Color.White, RoundedCornerShape(12.dp))
                    .border(
                        width = if (activo) 2.dp else 1.dp,
                        color = if (activo) rol.colorTexto else Color(0xFFDDE3EA),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .selectable(selected = activo, role = Role.RadioButton, onClick = { onSeleccionar(rol) })
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(rol.icono, contentDescription = null, tint = rol.colorTexto, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(rol.nombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AzulOscuro)
                    Text(rol.descripcion, fontSize = 12.sp, color = TextoSecundario)
                }
                RadioButton(
                    selected = activo,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(selectedColor = rol.colorTexto)
                )
            }
        }
    }
}

/** Se muestra si alguien llega a una pantalla que su rol no permite (p. ej. por un enlace). */
@Composable
fun AccesoRestringido(rol: Rol, accion: String, onVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(titulo = "Acceso restringido", subtitulo = rol.nombre, onVolver = onVolver)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(56.dp))
            Spacer(Modifier.size(16.dp))
            Text(
                text = "Tu rol (${rol.nombre}) no permite $accion.",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AzulOscuro,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "Si necesitas este acceso, pídele a un administrador que cambie tu rol.",
                fontSize = 13.sp,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectorRolPreview() {
    ElectraTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EtiquetaRol(Rol.SUPERVISOR)
            SelectorRol(seleccionado = Rol.TECNICO, onSeleccionar = {})
        }
    }
}
