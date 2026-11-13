package com.example.electra.data

/**
 * Qué pruebas y protocolos le corresponden a quien tiene la sesión abierta.
 *
 * - [usuario]: login de la sesión; queda guardado como creador de lo que registre.
 * - [veTodo]: true para los roles que revisan el trabajo de otros (ver Rol.veRegistrosDeTodos);
 *   si es false solo se ven los registros creados por [usuario].
 * - [desde]: momento en que se creó la cuenta (epoch ms). Quien ve todo solo ve lo registrado desde
 *   entonces, así toda cuenta nueva parte vacía sin importar su rol. null = sin límite (cuentas
 *   creadas antes de existir esta regla).
 *
 * Sin sesión y sin [veTodo] no se ve nada.
 */
data class Alcance(val usuario: String?, val veTodo: Boolean, val desde: Long? = null) {

    /** Filtro por creador para las consultas: null = sin filtro; si no, el login cuyos registros se muestran. */
    val creador: String? get() = if (veTodo) null else usuario.orEmpty()

    /** Filtro por fecha de creación para las consultas: null = sin filtro. */
    val creadosDesde: Long? get() = if (veTodo) desde else null

    fun puedeVer(creadoPor: String?, creadoEn: Long?): Boolean =
        if (veTodo) desde == null || (creadoEn != null && creadoEn >= desde)
        else !usuario.isNullOrBlank() && creadoPor == usuario

    companion object {
        /** Sin restricciones ni creador: para pruebas unitarias y vistas previas. */
        val TODO = Alcance(usuario = null, veTodo = true)
    }
}

/** Alcance de la sesión actual, según el rol del usuario conectado y la fecha de creación de su cuenta. */
suspend fun alcanceActual(sesion: SesionRepository, usuarios: UsuariosRepository): Alcance {
    val usuario = sesion.usuarioActual()?.takeIf { it.isNotBlank() }
        ?: return Alcance(usuario = null, veTodo = false)
    return Alcance(
        usuario = usuario,
        veTodo = usuarios.rol(usuario).veRegistrosDeTodos,
        desde = usuarios.creadaEn(usuario)
    )
}
