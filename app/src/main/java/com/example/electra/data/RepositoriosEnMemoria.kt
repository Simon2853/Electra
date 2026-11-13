package com.example.electra.data

import com.example.electra.model.Avatar
import com.example.electra.model.Rol
import com.example.electra.model.UsuarioResumen
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.Protocolo
import com.example.electra.model.Prueba
import com.example.electra.model.siguienteNumero


class PruebasRepositoryEnMemoria(
    iniciales: List<Prueba> = emptyList(),
    private val alcance: suspend () -> Alcance = { Alcance.TODO },
    private val reloj: () -> Long = System::currentTimeMillis
) : PruebasRepository {

    /** Todas las pruebas guardadas, sin filtrar por usuario. */
    val pruebas = iniciales.toMutableList()
    private var siguienteId = (iniciales.maxOfOrNull { it.id } ?: 0L) + 1

    private suspend fun visibles(): List<Prueba> {
        val alcance = alcance()
        return pruebas.filter { alcance.puedeVer(it.creadoPor, it.creadoEn) }
    }

    override suspend fun insertar(prueba: Prueba): Long {
        val id = siguienteId++
        pruebas.add(
            prueba.copy(id = id, creadoPor = alcance().usuario ?: prueba.creadoPor, creadoEn = reloj())
        )
        return id
    }

    override suspend fun buscarPorId(id: Long): Prueba? = visibles().firstOrNull { it.id == id }

    override suspend fun listarPorEquipo(codigoEquipo: String): List<Prueba> =
        visibles().filter { it.equipo.trim() == codigoEquipo.trim() }.sortedByDescending { it.id }

    override suspend fun listarTodas(): List<Prueba> = visibles().sortedByDescending { it.id }

    override suspend fun equiposConRegistros(): List<String> =
        visibles().map { it.equipo }.distinct().sorted()
            .ifEmpty { CatalogoEquipos.CODIGOS_POR_DEFECTO }
}

class ProtocolosRepositoryEnMemoria(
    iniciales: List<Protocolo> = emptyList(),
    private val alcance: suspend () -> Alcance = { Alcance.TODO },
    private val reloj: () -> Long = System::currentTimeMillis
) : ProtocolosRepository {

    /** Todos los protocolos guardados, sin filtrar por usuario. */
    val protocolos = iniciales.toMutableList()
    private var siguienteId = (iniciales.maxOfOrNull { it.id } ?: 0L) + 1

    private fun normalizar(numero: String) = "#" + numero.trim().removePrefix("#")

    private fun buscarEnTodos(numero: String): Protocolo? =
        protocolos.firstOrNull { normalizar(it.numero) == normalizar(numero) }

    override suspend fun listar(): List<Protocolo> {
        val alcance = alcance()
        return protocolos.filter { alcance.puedeVer(it.creadoPor, it.creadoEn) }.sortedByDescending { it.id }
    }

    override suspend fun buscar(numero: String): Protocolo? {
        val alcance = alcance()
        return buscarEnTodos(numero)?.takeIf { alcance.puedeVer(it.creadoPor, it.creadoEn) }
    }

    override suspend fun siguienteNumero(): String = siguienteNumero(protocolos)

    override suspend fun insertar(protocolo: Protocolo): Boolean {
        // El número es único entre todos los usuarios, no solo entre los visibles.
        if (buscarEnTodos(protocolo.numero) != null) return false
        protocolos.add(
            protocolo.copy(
                id = siguienteId++,
                numero = normalizar(protocolo.numero),
                creadoPor = alcance().usuario ?: protocolo.creadoPor,
                creadoEn = reloj()
            )
        )
        return true
    }

    override suspend fun cambiarEstado(numero: String, estado: String): Boolean =
        actualizar(numero) { it.copy(estado = estado.trim()) }

    override suspend fun actualizarObservaciones(numero: String, observaciones: String): Boolean =
        actualizar(numero) { it.copy(observaciones = observaciones.trim()) }

    private suspend fun actualizar(numero: String, cambio: (Protocolo) -> Protocolo): Boolean {
        val actual = buscar(numero) ?: return false
        protocolos[protocolos.indexOf(actual)] = cambio(actual)
        return true
    }
}

class UsuariosRepositoryEnMemoria(
    private val reloj: () -> Long = System::currentTimeMillis
) : UsuariosRepository {

    private val usuarios = mutableMapOf<String, Pair<String, String>>()
    private val avatares = mutableMapOf<String, Avatar>()
    private val roles = mutableMapOf<String, Rol>()
    private val creadas = mutableMapOf<String, Long>()

    override suspend fun registrar(nombre: String, usuario: String, contrasena: String, rol: Rol): Boolean {
        val clave = usuario.trim()
        if (clave.isBlank() || clave in usuarios) return false
        usuarios[clave] = nombre.trim() to contrasena
        roles[clave] = rol
        creadas[clave] = reloj()
        return true
    }

    override suspend fun creadaEn(usuario: String): Long? = creadas[usuario.trim()]

    override suspend fun rol(usuario: String): Rol = roles[usuario.trim()] ?: Rol.POR_DEFECTO

    override suspend fun actualizarRol(usuario: String, rol: Rol): Boolean {
        val clave = usuario.trim()
        if (clave !in usuarios) return false
        roles[clave] = rol
        return true
    }

    override suspend fun listar(): List<UsuarioResumen> =
        usuarios.map { (usuario, datos) -> UsuarioResumen(usuario, datos.first, rol(usuario), avatar(usuario)) }
            .sortedBy { it.nombre.lowercase() }

    override suspend fun validar(usuario: String, contrasena: String): Boolean =
        usuarios[usuario.trim()]?.second == contrasena

    override suspend fun restablecer(usuario: String, nombre: String, nueva: String): Boolean {
        val clave = usuario.trim()
        val (registrado, _) = usuarios[clave] ?: return false
        if (!registrado.equals(nombre.trim(), ignoreCase = true)) return false
        usuarios[clave] = registrado to nueva
        return true
    }

    override suspend fun cambiarContrasena(usuario: String, actual: String, nueva: String): Boolean {
        val clave = usuario.trim()
        val (nombre, guardada) = usuarios[clave] ?: return false
        if (guardada != actual) return false
        usuarios[clave] = nombre to nueva
        return true
    }

    override suspend fun actualizarNombre(usuario: String, nombre: String): Boolean {
        val clave = usuario.trim()
        val (_, contrasena) = usuarios[clave] ?: return false
        usuarios[clave] = nombre.trim() to contrasena
        return true
    }

    override suspend fun avatar(usuario: String): Avatar = avatares[usuario.trim()] ?: Avatar()

    override suspend fun actualizarAvatar(usuario: String, avatar: Avatar): Boolean {
        val clave = usuario.trim()
        if (clave !in usuarios) return false
        avatares[clave] = avatar
        return true
    }

    override suspend fun nombre(usuario: String): String? = usuarios[usuario.trim()]?.first
}

class SesionEnMemoria(private var usuario: String? = null) : SesionRepository {
    override fun usuarioActual(): String? = usuario
    override fun iniciar(usuario: String) {
        this.usuario = usuario
    }
    override fun cerrar() {
        usuario = null
    }
}
