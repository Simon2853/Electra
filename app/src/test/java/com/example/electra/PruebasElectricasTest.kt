package com.example.electra

import com.example.electra.data.ClimaFijo
import com.example.electra.data.ProtocolosRepositoryEnMemoria
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.data.SesionEnMemoria
import com.example.electra.data.UsuariosRepositoryEnMemoria
import com.example.electra.data.remote.ActualOpenMeteo
import com.example.electra.data.remote.ArchivosRemoto
import com.example.electra.data.remote.Conexion
import com.example.electra.data.remote.ConfiguracionApi
import com.example.electra.data.remote.RemotoClimaRepository
import com.example.electra.data.remote.RemotoPruebasRepository
import com.example.electra.data.remote.RespuestaOpenMeteo
import com.example.electra.device.Coordenadas
import com.example.electra.device.ResultadoUbicacion
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.Clima
import com.example.electra.model.Respaldo
import com.example.electra.ui.viewmodel.DetallePruebaViewModel
import com.example.electra.ui.viewmodel.EstadoClima
import com.example.electra.ui.viewmodel.EstadoUbicacion
import com.example.electra.ui.viewmodel.HistorialEquipoViewModel
import com.example.electra.ui.viewmodel.HomeViewModel
import com.example.electra.ui.viewmodel.RegistrarPruebaViewModel
import com.example.electra.ui.viewmodel.compararConAnterior
import com.example.electra.ui.viewmodel.ultimosPorEquipo
import com.example.electra.validation.Validaciones
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * PRUEBAS ELÉCTRICAS: registrar una prueba (con GPS y clima), historial por equipo, comparación con
 * la medición anterior, respaldo para compartir y resumen de la pantalla de inicio.
 * Las pruebas "remoto_" usan pruebas-service y archivos-service falsos, y una Open-Meteo falsa.
 */
class PruebasElectricasTest {

    // Hace que viewModelScope funcione en las pruebas
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val ahora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse("23/10/2026 12:00")!!
    private val santiago = Coordenadas(-33.45, -70.66)
    private val lluvioso = Clima(temperatura = 9.5, humedad = 91, codigo = 61)

    private lateinit var repositorio: PruebasRepositoryEnMemoria

    // Para las pruebas contra el servidor falso
    private lateinit var api: PruebasApiFalsa
    private lateinit var archivosApi: ArchivosApiFalsa
    private val fotosLocalesBorradas = mutableListOf<String>()
    private var sesionActual: String? = "camila"
    private lateinit var remoto: RemotoPruebasRepository

    @Before
    fun preparar() {
        repositorio = PruebasRepositoryEnMemoria()

        api = PruebasApiFalsa()
        archivosApi = ArchivosApiFalsa()
        val conexion = Conexion()
        val archivos = ArchivosRemoto(archivosApi, conexion) { byteArrayOf(1, 2, 3) }
        remoto = RemotoPruebasRepository(api, conexion, archivos, { sesionActual }, eliminarLocal = { fotosLocalesBorradas += it })
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun registrarPrueba(clima: Clima? = null) = RegistrarPruebaViewModel(
        repositorio,
        { ResultadoUbicacion.Exito(santiago) },
        "TR-001",
        ClimaFijo(clima)
    ) { ahora }

    // ---------------------------------------------------------------- registrar prueba

    @Test
    fun guardar_conMedicionConComa_guardaLaMedicionNormalizada() {
        val viewModel = registrarPrueba()
        viewModel.onMedicionChange("12,5")

        viewModel.guardar()

        val guardada = repositorio.pruebas.single()
        assertEquals("TR-001", guardada.equipo)
        assertEquals("12.5", guardada.medicion)
    }

    @Test
    fun guardar_rechazadaSinObservaciones_exigeElMotivo() {
        val viewModel = registrarPrueba()
        viewModel.onMedicionChange("0.5")
        viewModel.onResultadoChange("Rechazado")

        viewModel.guardar()

        assertEquals("Describe el motivo del rechazo.", viewModel.uiState.value.errorObservaciones)
        assertTrue(repositorio.pruebas.isEmpty())
    }

    @Test
    fun guardar_conFechaFutura_noGuarda() {
        val viewModel = registrarPrueba()
        viewModel.onMedicionChange("100")
        viewModel.onFechaChange("24/10/2026")

        viewModel.guardar()

        assertEquals("La prueba no puede tener fecha u hora futura.", viewModel.uiState.value.errorFecha)
        assertTrue(repositorio.pruebas.isEmpty())
    }

    @Test
    fun capturarUbicacion_guardaLasCoordenadasYConsultaElClima() {
        val viewModel = registrarPrueba(clima = lluvioso)

        viewModel.capturarUbicacion()

        val estado = viewModel.uiState.value
        assertEquals(EstadoUbicacion.CAPTURADA, estado.estadoUbicacion)
        assertEquals(santiago, estado.coordenadas)
        assertEquals(EstadoClima.LISTO, estado.estadoClima)
        assertTrue(estado.clima!!.condicionesAdversas)
    }

    @Test
    fun capturarUbicacion_siElClimaNoResponde_permiteRegistrarIgual() {
        val viewModel = registrarPrueba(clima = null)
        viewModel.capturarUbicacion()
        viewModel.onMedicionChange("100")

        viewModel.guardar()

        assertEquals(EstadoClima.NO_DISPONIBLE, viewModel.uiState.value.estadoClima)
        assertEquals(-33.45, repositorio.pruebas.single().latitud!!, 0.0)
    }

    @Test
    fun agregarClimaAObservaciones_loAgregaUnaSolaVezDespuesDelTexto() {
        val viewModel = registrarPrueba(clima = lluvioso)
        viewModel.capturarUbicacion()
        viewModel.onObservacionesChange("Aislación húmeda")

        viewModel.agregarClimaAObservaciones()
        viewModel.agregarClimaAObservaciones() // la segunda vez no duplica

        assertEquals("Aislación húmeda\n${lluvioso.textoObservacion}", viewModel.uiState.value.observaciones)
        assertTrue(viewModel.uiState.value.climaEnObservaciones)
    }

    @Test
    fun capturarUbicacion_sinPermiso_noConsultaElClima() {
        val viewModel = RegistrarPruebaViewModel(repositorio, { ResultadoUbicacion.SinPermiso }, "TR-001", ClimaFijo(lluvioso)) { ahora }

        viewModel.capturarUbicacion()

        assertEquals(EstadoUbicacion.SIN_PERMISO, viewModel.uiState.value.estadoUbicacion)
        assertEquals(EstadoClima.SIN_CONSULTAR, viewModel.uiState.value.estadoClima)
    }

    // ---------------------------------------------------------------- clima (regla de negocio)

    @Test
    fun clima_conHumedadAltaOLluvia_esCondicionAdversa() {
        assertTrue(Clima(15.0, humedad = 80, codigo = 0).condicionesAdversas) // justo en el umbral
        assertFalse(Clima(15.0, humedad = 79, codigo = 0).condicionesAdversas)
        assertTrue(Clima(15.0, humedad = 40, codigo = 95).condicionesAdversas) // tormenta
        assertFalse(Clima(15.0, humedad = 40, codigo = 3).condicionesAdversas) // nublado no basta
    }

    @Test
    fun clima_traduceLosCodigosWmoYArmaElResumen() {
        assertEquals("Despejado", Clima.descripcionDe(0))
        assertEquals("Niebla", Clima.descripcionDe(45))
        assertEquals("Llovizna", Clima.descripcionDe(53))
        assertEquals("Nieve", Clima.descripcionDe(73))
        assertEquals("Sin datos", Clima.descripcionDe(-1))
        assertEquals("9,5 °C · 91 % humedad · Lluvia", lluvioso.resumen)
    }

    // ---------------------------------------------------------------- historial, detalle e inicio

    @Test
    fun cargar_conPruebasDeVariosEquipos_muestraSoloLasDelEquipo() {
        val viewModel = HistorialEquipoViewModel(
            PruebasRepositoryEnMemoria(listOf(prueba(1, "TR-001"), prueba(2, "RL-004"), prueba(3, "TR-001"))), "TR-001"
        )

        viewModel.cargar()
        assertEquals(listOf(3L, 1L), viewModel.uiState.value.pruebas.map { it.id })

        viewModel.seleccionarEquipo("RL-004")
        assertEquals(listOf(2L), viewModel.uiState.value.pruebas.map { it.id })
    }

    @Test
    fun compararConAnterior_calculaLaDiferenciaConLaMismaPruebaAnterior() {
        val anterior = prueba(1, medicion = "1000")
        val actual = prueba(2, medicion = "1200")

        val comparacion = compararConAnterior(actual, listOf(actual, anterior))!!

        assertEquals(200.0, comparacion.diferencia, 0.001)
        assertEquals(20.0, comparacion.porcentaje!!, 0.001)
        assertNull(compararConAnterior(anterior, listOf(anterior))) // sin una anterior no se compara
    }

    @Test
    fun detallePrueba_cargaLaPruebaYSuComparacion() {
        val viewModel = DetallePruebaViewModel(
            PruebasRepositoryEnMemoria(listOf(prueba(1, medicion = "1000"), prueba(2, medicion = "900"))), 2
        )

        assertEquals("900", viewModel.uiState.value.prueba?.medicion)
        assertEquals(-10.0, viewModel.uiState.value.comparacion!!.porcentaje!!, 0.001)
        assertTrue(viewModel.textoRespaldo()!!.contains("Medición: 900 MΩ"))
    }

    @Test
    fun respaldo_incluyeEquipoInstalacionYUbicacion() {
        val conGps = prueba(1).copy(latitud = -33.45, longitud = -70.66, observaciones = "OK", creadoPor = "camila")

        val texto = Respaldo.dePrueba(conGps)

        assertTrue(texto.contains("Instalación: Subestación Norte (Empresa Eléctrica Norte)"))
        assertTrue(texto.contains("Ubicación GPS: -33.45000, -70.66000"))
        assertTrue(texto.contains("Registrado por: camila"))
        assertNull(Respaldo.coordenadas(prueba(2)))
    }

    @Test
    fun inicio_cargar_resumeLasPruebasProtocolosYElUsuario() = runBlocking {
        val usuarios = UsuariosRepositoryEnMemoria().apply { registrar("Camila Rojas", "camila", "Segura#2026") }
        val viewModel = HomeViewModel(
            PruebasRepositoryEnMemoria(listOf(prueba(1), prueba(2), prueba(3), prueba(4))),
            ProtocolosRepositoryEnMemoria(listOf(protocolo(1, "#000001"))),
            usuarios,
            SesionEnMemoria("camila")
        )

        viewModel.cargar()

        val estado = viewModel.uiState.value
        assertEquals("Camila", estado.nombre)
        assertEquals(4, estado.totalPruebas)
        assertEquals(1, estado.protocolosPendientes)
        assertEquals(3, estado.recientes.size) // solo las 3 más recientes
    }

    @Test
    fun ultimosPorEquipo_dejaComoMaximoElLimitePorCadaEquipo() {
        val lista = listOf("A", "A", "A", "B", "A", "B")

        assertEquals(listOf("A", "A", "B", "B"), lista.ultimosPorEquipo(limite = 2) { it })
    }

    @Test
    fun medicion_validaNumeroSignoYRango() {
        assertEquals("Ingresa el valor medido.", Validaciones.medicion(" "))
        assertEquals("La medición debe ser un número (ej: 125.5).", Validaciones.medicion("12a"))
        assertEquals("La medición no puede ser negativa.", Validaciones.medicion("-1"))
        assertEquals("El valor parece fuera de rango (máx. 1.000.000).", Validaciones.medicion("2000000"))
        assertNull(Validaciones.medicion("125,5"))
    }

    // ---------------------------------------------------------------- pruebas-service y archivos-service (falsos)

    @Test
    fun remoto_insertar_subeLaFotoYRegistraLaPruebaConSuRuta() = runBlocking {
        val nueva = prueba(medicion = "12,5").copy(fotoUri = "content://foto.jpg", latitud = -33.45, longitud = -70.66)

        val id = remoto.insertar(nueva)

        assertEquals(1L, id)
        assertEquals("/api/archivos/foto1", api.ultimaRecibida?.fotoUrl)
        assertEquals("12.5", api.ultimaRecibida?.medicion)
        assertEquals("camila", api.ultimaRecibida?.creadoPor) // el creador es quien tiene la sesión
        assertEquals(listOf("content://foto.jpg"), fotosLocalesBorradas)
    }

    @Test
    fun remoto_insertar_siNoSePuedeSubirLaFotoNoRegistraNada() = runBlocking {
        archivosApi.caido = true

        assertNull(remoto.insertar(prueba().copy(fotoUri = "content://foto.jpg")))
        assertTrue(api.guardadas.isEmpty())
        assertTrue(fotosLocalesBorradas.isEmpty())
    }

    @Test
    fun remoto_consultas_conviertenLaRutaDeLaFotoEnUrlCompleta() = runBlocking {
        remoto.insertar(prueba(equipo = "TR-001").copy(fotoUri = "content://a.jpg"))
        remoto.insertar(prueba(equipo = "RL-004"))

        val delEquipo = remoto.listarPorEquipo("TR-001").single()
        assertEquals(ConfiguracionApi.urlCompleta("/api/archivos/foto1"), delEquipo.fotoUri)
        assertEquals(2, remoto.listarTodas().size)
        assertEquals("RL-004", remoto.buscarPorId(2)?.equipo)
        assertNull(remoto.buscarPorId(99))
        assertEquals(listOf("TR-001", "RL-004"), remoto.equiposConRegistros())
    }

    @Test
    fun remoto_sinSesionOSinRegistros_noConsultaYMuestraElCatalogo() = runBlocking {
        assertEquals(CatalogoEquipos.CODIGOS_POR_DEFECTO, remoto.equiposConRegistros()) // aún no hay pruebas

        sesionActual = null
        assertTrue(remoto.listarTodas().isEmpty())
        assertNull(remoto.buscarPorId(1))
    }

    // ---------------------------------------------------------------- API externa Open-Meteo (falsa)

    @Test
    fun remoto_clima_convierteLaRespuestaDeOpenMeteo() = runBlocking {
        val openMeteo = OpenMeteoFalsa { respuesta(200, RespuestaOpenMeteo(ActualOpenMeteo(18.4, 62.6, 2))) }

        val clima = RemotoClimaRepository(openMeteo).actual(-33.45, -70.66)

        assertEquals(Clima(temperatura = 18.4, humedad = 63, codigo = 2), clima) // la humedad se redondea
    }

    @Test
    fun remoto_clima_sinRedORespuestaIncompleta_devuelveNull() = runBlocking {
        val caida = OpenMeteoFalsa { sinRed() }
        val incompleta = OpenMeteoFalsa { respuesta(200, RespuestaOpenMeteo(ActualOpenMeteo(null, 50.0, 0))) }
        val conError = OpenMeteoFalsa { errorHttp(500) }

        assertNull(RemotoClimaRepository(caida).actual(0.0, 0.0))
        assertNull(RemotoClimaRepository(incompleta).actual(0.0, 0.0))
        assertNull(RemotoClimaRepository(conError).actual(0.0, 0.0))
    }
}
