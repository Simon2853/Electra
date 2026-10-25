package com.example.electra.data

import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.Protocolo
import com.example.electra.model.Prueba


class PruebasRepositoryEnMemoria(
    iniciales: List<Prueba> = emptyList()
) : PruebasRepository {

    val pruebas = iniciales.toMutableList()
    private var siguienteId = (iniciales.maxOfOrNull { it.id } ?: 0L) + 1

    override suspend fun insertar(prueba: Prueba): Long {
        val id = siguienteId++
        pruebas.add(prueba.copy(id = id))
        return id
    }

    override suspend fun buscarPorId(id: Long): Prueba? = pruebas.firstOrNull { it.id == id }

    override suspend fun listarPorEquipo(codigoEquipo: String): List<Prueba> =
        pruebas.filter { it.equipo.trim() == codigoEquipo.trim() }.sortedByDescending { it.id }

    override suspend fun listarTodas(): List<Prueba> = pruebas.sortedByDescending { it.id }

    override suspend fun equiposConRegistros(): List<String> =
        pruebas.map { it.equipo }.distinct().sorted()
            .ifEmpty { CatalogoEquipos.CODIGOS_POR_DEFECTO }
}

class ProtocolosRepositoryEnMemoria(
    iniciales: List<Protocolo> = emptyList()
) : ProtocolosRepository {

    val protocolos = iniciales.toMutableList()
    private var siguienteId = (iniciales.maxOfOrNull { it.id } ?: 0L) + 1

    private fun normalizar(numero: String) = "#" + numero.trim().removePrefix("#")

    override suspend fun listar(): List<Protocolo> = protocolos.sortedByDescending { it.id }

    override suspend fun buscar(numero: String): Protocolo? =
        protocolos.firstOrNull { normalizar(it.numero) == normalizar(numero) }

    override suspend fun insertar(protocolo: Protocolo): Boolean {
        if (buscar(protocolo.numero) != null) return false
        protocolos.add(protocolo.copy(id = siguienteId++, numero = normalizar(protocolo.numero)))
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

class UsuariosRepositoryEnMemoria : UsuariosRepository {

    private val usuarios = mutableMapOf<String, Pair<String, String>>()

    override suspend fun registrar(nombre: String, usuario: String, contrasena: String): Boolean {
        val clave = usuario.trim()
        if (clave.isBlank() || clave in usuarios) return false
        usuarios[clave] = nombre.trim() to contrasena
        return true
    }

    override suspend fun validar(usuario: String, contrasena: String): Boolean =
        usuarios[usuario.trim()]?.second == contrasena

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
