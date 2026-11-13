package com.example.electra.model

/**
 * Roles de usuario y lo que cada uno puede hacer. Es la ÚNICA fuente de verdad de permisos:
 * pantallas y ViewModels preguntan aquí (rol.puedeFormalizar, etc.) en vez de comparar roles.
 *
 * | Permiso              | Técnico | Supervisor | Admin |
 * |----------------------|:-------:|:----------:|:-----:|
 * | Ver equipos/historial|   ✔     |     ✔      |   ✔   |
 * | Registrar prueba     |   ✔     |     ✘      |   ✔   |
 * | Crear protocolo      |   ✔     |     ✘      |   ✔   |
 * | Observaciones        |   ✔     |     ✔      |   ✔   |
 * | Formalizar protocolo |   ✘     |     ✔      |   ✔   |
 * | Gestionar usuarios   |   ✘     |     ✘      |   ✔   |
 * | Ver registros ajenos |   ✘     |     ✔      |   ✔   |
 *
 * Quien formaliza no genera los datos que revisa (separación de funciones).
 * "clave" es lo que se guarda en el almacenamiento: no cambiarla.
 */
enum class Rol(
    val clave: String,
    val nombre: String,
    val descripcion: String,
    val puedeRegistrarPruebas: Boolean,
    val puedeCrearProtocolos: Boolean,
    val puedeFormalizar: Boolean,
    val puedeGestionarUsuarios: Boolean
) {
    TECNICO(
        clave = "tecnico",
        nombre = "Técnico de terreno",
        descripcion = "Registra pruebas y crea protocolos en terreno.",
        puedeRegistrarPruebas = true,
        puedeCrearProtocolos = true,
        puedeFormalizar = false,
        puedeGestionarUsuarios = false
    ),
    SUPERVISOR(
        clave = "supervisor",
        nombre = "Supervisor / Ingeniero",
        descripcion = "Revisa los protocolos y los formaliza.",
        puedeRegistrarPruebas = false,
        puedeCrearProtocolos = false,
        puedeFormalizar = true,
        puedeGestionarUsuarios = false
    ),
    ADMINISTRADOR(
        clave = "admin",
        nombre = "Administrador",
        descripcion = "Acceso completo y gestión de usuarios y roles.",
        puedeRegistrarPruebas = true,
        puedeCrearProtocolos = true,
        puedeFormalizar = true,
        puedeGestionarUsuarios = true
    );

    val puedeEditarObservaciones: Boolean get() = true

    /**
     * Si ve las pruebas y protocolos de todos los usuarios o solo los que creó él mismo.
     * Quien formaliza revisa el trabajo de otros, así que necesita verlo; el técnico solo ve lo suyo.
     */
    val veRegistrosDeTodos: Boolean get() = puedeFormalizar

    /** Lista legible de permisos, para mostrar en el perfil. */
    val permisos: List<Pair<String, Boolean>>
        get() = listOf(
            "Registrar pruebas" to puedeRegistrarPruebas,
            "Crear protocolos" to puedeCrearProtocolos,
            "Formalizar protocolos" to puedeFormalizar,
            "Gestionar usuarios" to puedeGestionarUsuarios
        )

    companion object {
        /** Cuentas creadas antes de existir los roles no tienen uno guardado: quedan como técnico. */
        val POR_DEFECTO = TECNICO

        fun desdeClave(clave: String?): Rol = entries.firstOrNull { it.clave == clave } ?: POR_DEFECTO
    }
}

data class UsuarioResumen(
    val usuario: String,
    val nombre: String,
    val rol: Rol,
    val avatar: Avatar = Avatar()
)
