package com.example.electra.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class ValidacionesTest {

    @Test
    fun contrasenaRegistro_reglas() {
        val v = Validaciones
        assertEquals("Ingresa una contraseña.", v.contrasenaRegistro("Ana", "anat", ""))
        assertEquals("La clave no puede superar los 20 caracteres.", v.contrasenaRegistro("Ana", "anat", "Segura#2026Segura#2026"))
        assertEquals("La clave debe incluir al menos una letra mayúscula.", v.contrasenaRegistro("Ana", "anat", "segura#2026"))
        assertEquals("La clave debe incluir al menos una letra minúscula.", v.contrasenaRegistro("Ana", "anat", "SEGURA#2026"))
        assertEquals("La clave debe incluir al menos un número.", v.contrasenaRegistro("Ana", "anat", "Segura#clave"))
        assertEquals("La clave debe incluir un carácter especial: ! @ # $ % & * ?", v.contrasenaRegistro("Ana", "anat", "Segura2026"))
        assertEquals("No incluyas el nombre de la aplicación.", v.contrasenaRegistro("Ana", "anat", "Electra#2026"))
        assertEquals("No incluyas tu nombre ni tu usuario.", v.contrasenaRegistro("Marcela", "anat", "Marcela#2026"))
        assertNull(v.contrasenaRegistro("Ana", "anat", "Segura#2026"))
    }

    @Test
    fun nombreYUsuario() {
        assertEquals("Ingresa tu nombre completo.", Validaciones.nombre("  "))
        assertEquals("El nombre solo puede contener letras.", Validaciones.nombre("Ana 123"))
        assertNull(Validaciones.nombre("José Pérez"))

        assertEquals("El usuario debe tener al menos 4 caracteres.", Validaciones.usuarioRegistro("ana"))
        assertEquals("Usa solo letras, números, punto, guion o guion bajo.", Validaciones.usuarioRegistro("ana#1"))
        assertNull(Validaciones.usuarioRegistro("j.perez_1"))
    }

    @Test
    fun confirmacion() {
        assertEquals("Confirma tu contraseña.", Validaciones.confirmacion("a", ""))
        assertEquals("Las contraseñas no coinciden.", Validaciones.confirmacion("a", "b"))
        assertNull(Validaciones.confirmacion("a", "a"))
    }

    @Test
    fun medicion() {
        assertEquals("Ingresa el valor medido.", Validaciones.medicion(" "))
        assertEquals("La medición debe ser un número (ej: 125.5).", Validaciones.medicion("1.2.3"))
        assertEquals("El valor parece fuera de rango (máx. 1.000.000).", Validaciones.medicion("2000000"))
        assertNull(Validaciones.medicion("0"))
        assertNull(Validaciones.medicion("125,5"))
    }

    @Test
    fun fechaPrueba() {
        val ahora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse("23/10/2026 12:00")!!
        assertNull(Validaciones.fechaPrueba("23/10/2026", "11:59", ahora))
        assertEquals("La prueba no puede tener fecha u hora futura.", Validaciones.fechaPrueba("23/10/2026", "12:01", ahora))
        assertEquals("Fecha u hora inválida.", Validaciones.fechaPrueba("31/02/2026", "10:00", ahora))
    }

    @Test
    fun protocolo() {
        assertEquals("Ingresa el número del protocolo.", Validaciones.numeroProtocolo(""))
        assertEquals("Usa 6 dígitos, por ejemplo #000130.", Validaciones.numeroProtocolo("#12"))
        assertNull(Validaciones.numeroProtocolo("#000130"))
        assertNull(Validaciones.numeroProtocolo("000130"))

        assertEquals("El nombre debe tener al menos 3 letras.", Validaciones.responsable("Al"))
        assertNull(Validaciones.responsable("Juan Pérez"))

        assertEquals("Máximo 300 caracteres.", Validaciones.observaciones("x".repeat(301)))
        assertNull(Validaciones.observaciones("x".repeat(300)))
    }
}
