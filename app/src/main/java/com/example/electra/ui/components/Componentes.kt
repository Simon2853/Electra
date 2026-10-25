package com.example.electra.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.electra.ui.theme.AzulOscuro
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.RojoError
import com.example.electra.ui.theme.TextoSecundario
import com.example.electra.ui.theme.VerdeExito
import kotlin.math.roundToInt


@Composable
fun CampoValidado(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    valido: Boolean = false,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxCaracteres: Int? = null,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    accionExtra: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = { nuevo ->
                if (maxCaracteres == null || nuevo.length <= maxCaracteres) onCambio(nuevo)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("campo_$etiqueta"),
            label = { Text(etiqueta) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = if (error != null) RojoError else AzulPrincipal
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoEstadoCampo(error = error, valido = valido)
                    accionExtra?.invoke()
                }
            },
            isError = error != null,
            singleLine = singleLine,
            minLines = minLines,
            readOnly = readOnly,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            supportingText = maxCaracteres?.let {
                { Text("${valor.length}/$it", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedBorderColor = AzulPrincipal
            )
        )
        MensajeError(error)
    }
}


@Composable
private fun IconoEstadoCampo(error: String?, valido: Boolean) {
    AnimatedContent(
        targetState = when {
            error != null -> 1
            valido -> 2
            else -> 0
        },
        transitionSpec = { scaleIn(spring(Spring.DampingRatioMediumBouncy)) togetherWith scaleOut() },
        label = "iconoEstadoCampo"
    ) { estado ->
        when (estado) {
            1 -> Icon(Icons.Filled.Error, contentDescription = "Campo con error", tint = RojoError)
            2 -> Icon(Icons.Filled.CheckCircle, contentDescription = "Campo válido", tint = VerdeExito)
            else -> Spacer(Modifier.size(0.dp))
        }
    }
}


@Composable
fun MensajeError(error: String?, modifier: Modifier = Modifier) {

    var ultimo by remember { mutableStateOf(error.orEmpty()) }
    if (error != null) ultimo = error

    AnimatedVisibility(
        visible = error != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = RojoError,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(text = ultimo, color = RojoError, fontSize = 12.sp)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorCampo(
    valor: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var abierto by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = abierto,
            onExpandedChange = { abierto = it }
        ) {
            OutlinedTextField(
                value = valor,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .testTag("selector_$etiqueta"),
                label = { Text(etiqueta) },
                leadingIcon = {
                    Icon(icono, contentDescription = null, tint = if (error != null) RojoError else AzulPrincipal)
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto) },
                isError = error != null,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    errorContainerColor = Color.White
                )
            )
            ExposedDropdownMenu(
                expanded = abierto,
                onDismissRequest = { abierto = false }
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            onSeleccion(opcion)
                            abierto = false
                        }
                    )
                }
            }
        }
        MensajeError(error)
    }
}



@Composable
fun BotonCarga(
    texto: String,
    icono: ImageVector,
    cargando: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = AzulPrincipal
) {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (presionado) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "escalaBoton"
    )

    Button(
        onClick = onClick,
        enabled = !cargando,
        interactionSource = interaccion,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.6f),
            disabledContentColor = Color.White
        )
    ) {
        AnimatedContent(
            targetState = cargando,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "contenidoBoton"
        ) { estaCargando ->
            if (estaCargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icono, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = texto, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
fun BarraSuperior(
    titulo: String,
    subtitulo: String,
    onVolver: (() -> Unit)? = null,
    acciones: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onVolver != null) {
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = AzulOscuro
                )
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
            Text(text = subtitulo, fontSize = 12.sp, color = TextoSecundario)
        }
        acciones()
    }
}


@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        modifier = modifier,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = AzulOscuro
    )
}



@Composable
fun Modifier.sacudir(disparador: Int): Modifier {
    val desplazamiento = remember { Animatable(0f) }
    LaunchedEffect(disparador) {
        if (disparador > 0) {
            for (x in listOf(-14f, 14f, -9f, 9f, -4f, 0f)) {
                desplazamiento.animateTo(x, tween(durationMillis = 45))
            }
        }
    }
    return this.offset { IntOffset(desplazamiento.value.roundToInt(), 0) }
}
