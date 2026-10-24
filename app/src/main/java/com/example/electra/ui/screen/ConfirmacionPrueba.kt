package com.example.electra.ui.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ConfirmacionPrueba() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FA))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // -------------------------------------------------
        // ICONO DE CONFIRMACIÓN
        // -------------------------------------------------

        Card(
            modifier = Modifier.size(90.dp),
            shape = RoundedCornerShape(50.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            )
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Prueba registrada",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(58.dp)
                )
            }
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // -------------------------------------------------
        // MENSAJE PRINCIPAL
        // -------------------------------------------------

        Text(
            text = "Prueba registrada correctamente.",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF173B57),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Los resultados fueron guardados correctamente.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // -------------------------------------------------
        // INFORMACIÓN DE LA PRUEBA
        // -------------------------------------------------

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                ConfirmationRow(
                    label = "Equipo",
                    value = "TRA-001"
                )

                ConfirmationRow(
                    label = "Resultado",
                    value = "APROBADO",
                    valueColor = Color(0xFF2E7D32)
                )

                ConfirmationRow(
                    label = "Fecha",
                    value = "02/10/2026"
                )

                ConfirmationRow(
                    label = "Estado",
                    value = "Registrado en terreno",
                    valueColor = Color(0xFF1769AA)
                )
            }
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // -------------------------------------------------
        // GENERAR PROTOCOLO
        // -------------------------------------------------

        Button(
            onClick = {
                // Por ahora sin lógica
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1769AA)
            )
        ) {

            Icon(
                imageVector = Icons.Outlined.AssignmentTurnedIn,
                contentDescription = "Generar protocolo"
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "Generar protocolo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(
            modifier = Modifier.height(10.dp)
        )


        // -------------------------------------------------
        // VER PRUEBA
        // -------------------------------------------------

        OutlinedButton(
            onClick = {
                // Por ahora sin lógica
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp)
        ) {

            Icon(
                imageVector = Icons.Outlined.Visibility,
                contentDescription = "Ver prueba"
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "Ver prueba",
                fontSize = 16.sp
            )
        }


        Spacer(
            modifier = Modifier.height(10.dp)
        )


        // -------------------------------------------------
        // VOLVER AL INICIO
        // -------------------------------------------------

        OutlinedButton(
            onClick = {
                // Por ahora sin lógica
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp)
        ) {

            Icon(
                imageVector = Icons.Outlined.Home,
                contentDescription = "Volver al inicio"
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "Volver al inicio",
                fontSize = 16.sp
            )
        }


        Spacer(
            modifier = Modifier.height(16.dp
            )
        )


        Text(
            text = "ELECTRA • Induztek Ingeniería SpA",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}


// =====================================================
// FILA DE INFORMACIÓN
// =====================================================

@Composable
fun ConfirmationRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF173B57)
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = Color.Gray
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}


// =====================================================
// PREVIEW
// =====================================================

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ConfirmacionPruebaPreview() {
    ConfirmacionPrueba()
}