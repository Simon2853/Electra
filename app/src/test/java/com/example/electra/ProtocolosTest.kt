package com.example.electra

import com.example.electra.data.ProtocolosRepositoryEnMemoria
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.data.remote.Conexion
import com.example.electra.data.remote.ConfiguracionApi
import com.example.electra.data.remote.RemotoProtocolosRepository
import com.example.electra.data.remote.cuerpo
import com.example.electra.data.remote.respondio
import com.example.electra.model.EstadosProtocolo
import com.example.electra.model.Respaldo
import com.example.electra.model.Rol
import com.example.electra.model.siguienteNumero
import com.example.electra.ui.viewmodel.CrearProtocoloViewModel
import com.example.electra.ui.viewmodel.DetalleProtocoloViewModel
import com.example.electra.ui.viewmodel.MensajeProtocolo
import com.example.electra.ui.viewmodel.ProtocolosViewModel
import com.example.electra.ui.viewmodel.ResultadoProtocolo
import com.example.electra.validation.Validaciones
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * PROTOCOLOS: crear (número asignado por el servidor), listar, ver el detalle, formalizar y editar
 * observaciones según el rol. Las pruebas "remoto_" usan un protocolos-service falso; al final se
 * prueba la conexión con el servidor y la configuración de la URL.
 */
class ProtocolosTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var repositorio: ProtocolosRepositoryEnMemoria

    // Para las pruebas contra el servidor falso
    private lateinit var api: ProtocolosApiFalsa
    private var sesionActual: String? = "camila"
    private lateinit var remoto: RemotoProtocolosRepository

    @Before
    fun preparar() {
        repositorio = ProtocolosRepositoryEnMemoria(listOf(protocolo(1, "#000122")))

        api = ProtocolosApiFalsa()
        remoto = RemotoProtocolosRepository(api, Conexion()) { sesionActual }
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun crearProtocolo(responsable: String, repo: com.example.electra.data.ProtocolosRepository = repositorio) =
        CrearProtocoloViewModel(repo, PruebasRepositoryEnMemoria(), "", fecha = "23/10/2026").apply {
            onResponsableChange(responsable)
            guardar()
        }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun detalle(rol: Rol, repo: com.example.electra.data.ProtocolosRepository = repositorio, numero: String = "000122") =
        DetalleProtocoloViewModel(repo, PruebasRepositoryEnMemoria(listOf(prueba(1), prueba(2, "RL-004"))), numero) { rol }
            .apply { cargar() }

    // ---------------------------------------------------------------- crear

    @Test
    fun guardar_conDatosValidos_creaElProtocoloConElSiguienteNumero() {
        val viewModel = crearProtocolo("Camila Rojas")

        val guardado = repositorio.protocolos.last()
        assertEquals(ResultadoProtocolo.EXITO, viewModel.uiState.value.resultado)
        assertEquals("#000123", guardado.numero)
        assertEquals("Camila Rojas", guardado.responsable)
        assertEquals(EstadosProtocolo.EN_TERRENO, guardado.estado) // todo protocolo nace en terreno
    }

    @Test
    fun crear_muestraElNumeroReferencialYLosEquiposDelCatalogo() {
        val viewModel = CrearProtocoloViewModel(repositorio, PruebasRepositoryEnMemoria(), "", fecha = "23/10/2026")

        assertEquals("#000123", viewModel.uiState.value.numeroReferencial)
        assertEquals("TR-001", viewModel.uiState.value.equipo)
        assertTrue("CB-012" in viewModel.uiState.value.equipos)
    }

    @Test
    fun guardar_conResponsableInvalido_noCreaElProtocolo() {
        val viewModel = crearProtocolo("J4")

        assertEquals(ResultadoProtocolo.CAMPOS_INCOMPLETOS, viewModel.uiState.value.resultado)
        assertEquals("El nombre debe tener al menos 3 letras.", viewModel.uiState.value.errorResponsable)
        assertEquals(1, repositorio.protocolos.size)
    }

    @Test
    fun guardar_sinConexionConElServidor_informaElError() {
        api.caido = true

        val viewModel = crearProtocolo("Camila Rojas", repo = remoto)

        assertEquals("Se asigna al guardar", viewModel.uiState.value.numeroReferencial) // no se pudo anticipar
        assertEquals(ResultadoProtocolo.ERROR, viewModel.uiState.value.resultado)
        assertEquals(1, viewModel.uiState.value.intentosFallidos)
    }

    // ---------------------------------------------------------------- listar y detalle

    @Test
    fun cargar_conProtocolosGuardados_muestraLosMasRecientesPrimero() {
        val viewModel = ProtocolosViewModel(ProtocolosRepositoryEnMemoria(listOf(protocolo(1, "#000122"), protocolo(2, "#000123"))))

        viewModel.cargar()

        assertEquals(listOf("#000123", "#000122"), viewModel.protocolos.value.map { it.numero })
    }

    @Test
    fun formalizar_comoSupervisor_cambiaElEstado() {
        val viewModel = detalle(Rol.SUPERVISOR)

        viewModel.formalizar()

        assertTrue(viewModel.uiState.value.formalizado)
        assertEquals("Formalizado", viewModel.uiState.value.protocolo?.estado)
        assertEquals(MensajeProtocolo.FORMALIZADO, viewModel.uiState.value.mensaje)
    }

    @Test
    fun formalizar_comoTecnico_noTienePermiso() {
        val viewModel = detalle(Rol.TECNICO)

        viewModel.formalizar()

        assertEquals(MensajeProtocolo.SIN_PERMISO, viewModel.uiState.value.mensaje)
        assertFalse(viewModel.uiState.value.formalizado)
    }

    @Test
    fun detalle_muestraSoloLasPruebasDelEquipoDelProtocolo() {
        val viewModel = detalle(Rol.TECNICO)

        assertEquals(listOf("TR-001"), viewModel.uiState.value.pruebas.map { it.equipo })
        assertTrue(viewModel.textoRespaldo()!!.contains("PROTOCOLO #000122"))
    }

    @Test
    fun editarObservacion_guardaElTextoYAvisa() {
        val viewModel = detalle(Rol.TECNICO)

        viewModel.abrirEdicion()
        viewModel.onObservacionChange("Cambio de aceite pendiente")
        viewModel.guardarObservacion()

        assertEquals("Cambio de aceite pendiente", viewModel.uiState.value.protocolo?.observaciones)
        assertEquals(MensajeProtocolo.OBSERVACION_ACTUALIZADA, viewModel.uiState.value.mensaje)
        assertFalse(viewModel.uiState.value.editandoObservacion)
    }

    @Test
    fun editarObservacion_demasiadoLarga_noSeGuarda() {
        val viewModel = detalle(Rol.TECNICO)

        viewModel.abrirEdicion()
        viewModel.onObservacionChange("x".repeat(Validaciones.MAX_OBSERVACIONES + 1))
        viewModel.guardarObservacion()

        assertEquals("Máximo 300 caracteres.", viewModel.uiState.value.errorObservacion)
        assertEquals("", viewModel.uiState.value.protocolo?.observaciones)
    }

    @Test
    fun protocoloFormalizado_yaNoPermiteEditarObservaciones() {
        val formalizado = ProtocolosRepositoryEnMemoria(listOf(protocolo(1, "#000122", estado = EstadosProtocolo.FORMALIZADO)))

        val viewModel = detalle(Rol.ADMINISTRADOR, repo = formalizado)

        assertFalse(viewModel.uiState.value.puedeEditarObservaciones)
        assertTrue(detalle(Rol.TECNICO).uiState.value.puedeEditarObservaciones) // uno sin formalizar sí
    }

    @Test
    fun formalizar_siElServidorLoRechaza_muestraError() = runBlocking {
        remoto.insertar(protocolo(0, ""))
        val viewModel = detalle(Rol.SUPERVISOR, repo = remoto, numero = "#000001")
        api.caido = true

        viewModel.formalizar()

        assertEquals(MensajeProtocolo.ERROR_ACTUALIZAR, viewModel.uiState.value.mensaje)
    }

    // ---------------------------------------------------------------- reglas y respaldo

    @Test
    fun siguienteNumero_esElMayorMasUnoConSeisDigitos() {
        assertEquals("#000001", siguienteNumero(emptyList()))
        assertEquals("#000124", siguienteNumero(listOf(protocolo(1, "#000099"), protocolo(2, "#000123"))))
    }

    @Test
    fun respaldoDeProtocolo_incluyeLasPruebasDelEquipo() {
        val texto = Respaldo.deProtocolo(protocolo(1, "#000122").copy(observaciones = "OK"), listOf(prueba(1)))

        assertTrue(texto.contains("Responsable: Juan Pérez"))
        assertTrue(texto.contains("Observaciones: OK"))
        assertTrue(texto.contains("Resistencia de aislamiento: 1200 MΩ (Aprobado)"))
    }

    @Test
    fun responsable_soloAceptaNombresConLetras() {
        assertEquals("Ingresa el responsable de la inspección.", Validaciones.responsable(""))
        assertEquals("El nombre solo puede contener letras.", Validaciones.responsable("Juan 123"))
        assertNull(Validaciones.responsable("Juan Pérez"))
        assertNull(Validaciones.numeroProtocolo("#000130"))
        assertEquals("Usa 6 dígitos, por ejemplo #000130.", Validaciones.numeroProtocolo("130"))
    }

    // ---------------------------------------------------------------- protocolos-service (falso)

    @Test
    fun remoto_insertar_dejaQueElServidorAsigneElNumero() = runBlocking {
        assertEquals("#000001", remoto.siguienteNumero())

        assertTrue(remoto.insertar(protocolo(0, "#999999")))

        assertEquals("#000001", remoto.listar().single().numero) // el número que mandó la app se ignora
        assertEquals("camila", api.guardados.single().creadoPor)
    }

    @Test
    fun remoto_buscar_quitaLaAlmohadillaDeLaUrl() = runBlocking {
        remoto.insertar(protocolo(0, ""))

        assertEquals("TR-001", remoto.buscar("#000001")?.equipo)
        assertEquals("000001", api.numerosPedidos.single()) // "#" en una URL cortaría la ruta
    }

    @Test
    fun remoto_formalizado_rechazaCambiosConConflicto() = runBlocking {
        remoto.insertar(protocolo(0, ""))

        assertTrue(remoto.actualizarObservaciones("#000001", "Revisado"))
        assertTrue(remoto.cambiarEstado("#000001", EstadosProtocolo.FORMALIZADO))
        assertFalse(remoto.actualizarObservaciones("#000001", "Otra")) // 409
        assertFalse(remoto.cambiarEstado("#000001", EstadosProtocolo.EN_TERRENO)) // 409
        assertEquals("Revisado", remoto.buscar("#000001")?.observaciones)
    }

    @Test
    fun remoto_sinSesion_noConsultaNiModifica() = runBlocking {
        sesionActual = null

        assertTrue(remoto.listar().isEmpty())
        assertNull(remoto.buscar("#000001"))
        assertFalse(remoto.cambiarEstado("#000001", EstadosProtocolo.FORMALIZADO))
        assertFalse(remoto.actualizarObservaciones("#000001", "x"))
    }

    // ---------------------------------------------------------------- conexión y configuración

    @Test
    fun conexion_gatewaySinMicroservicio_activaElAviso() = runBlocking {
        val conexion = Conexion()

        val respuesta = conexion.llamar { errorHttp<Unit>(503) } // el Gateway responde, el servicio no

        assertTrue(conexion.sinConexion.value)
        assertFalse(respuesta.respondio(200))
        assertNull(respuesta.cuerpo())
    }

    @Test
    fun configuracionApi_armaYSeparaLasUrlDeLasFotos() {
        val url = ConfiguracionApi.urlCompleta("/api/archivos/abc")

        assertEquals(ConfiguracionApi.BASE_URL + "api/archivos/abc", url)
        assertEquals("/api/archivos/abc", ConfiguracionApi.rutaRelativa(url))
        assertEquals("https://otra.cl/x.jpg", ConfiguracionApi.urlCompleta("https://otra.cl/x.jpg"))
    }
}
