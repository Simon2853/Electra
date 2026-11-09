package com.example.electra.ui.styles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.electra.ui.theme.AzulPrincipal
import com.example.electra.ui.theme.Dimens

object EstilosBoton {
    val forma = RoundedCornerShape(12.dp)
    val relleno = PaddingValues(horizontal = 20.dp, vertical = 12.dp)

    @Composable
    fun coloresPrincipal() = ButtonDefaults.buttonColors(containerColor = AzulPrincipal, contentColor = Color.White)

    @Composable
    fun coloresSecundario() = ButtonDefaults.outlinedButtonColors(contentColor = AzulPrincipal)

    @Composable
    fun elevacion() = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp)

    fun bordeSecundario() = BorderStroke(2.dp, AzulPrincipal)
}

fun Modifier.estiloAltoBoton(): Modifier = height(Dimens.altoBoton)

object EstilosCampo {
    val forma = RoundedCornerShape(12.dp)

    @Composable
    fun colores() = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        errorContainerColor = Color.White,
        focusedBorderColor = AzulPrincipal
    )
}

object EstilosTarjeta {
    val forma = RoundedCornerShape(16.dp)

    @Composable
    fun colores() = CardDefaults.cardColors(containerColor = Color.White)

    @Composable
    fun elevacion() = CardDefaults.cardElevation(defaultElevation = 2.dp)
}
