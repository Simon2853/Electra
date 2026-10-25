package com.example.electra.ui.viewmodel

import com.example.electra.data.PruebasRepository
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.device.Coordenadas
import com.example.electra.device.ProveedorUbicacion
import com.example.electra.device.ResultadoUbicacion
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.OpcionesPrueba
import com.example.electra.model.Prueba
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class PruebasViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val ahora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse("23/10/2026 12:00")!!

    private val gpsOk = ProveedorUbicacion { ResultadoUbicacion.Exito(Coordenadas(-33.36, -70.68)) }

    private fun registrarVm(
        repo: PruebasRepository = PruebasRepositoryEnMemoria(),
        equipo: String = "TR-001",
        ubicacion: ProveedorUbicacion = gpsOk
    ) = RegistrarPruebaViewModel(repo, ubicacion, equipo) { ahora }

    @Test
    fun registrar_iniciaConFechaYHoraActuales() {
        val vm = registrarVm()

        assertEquals("23/10/2026", vm.uiState.value.fecha)
        assertEquals("12:00", vm.uiState.value.hora)
        assertNull(vm.uiState.value.errorMedicion)
    }

    @Test
    fun registrar_medicionVaciaMarcaErrorDelCampoYNoGuarda() {
        val repo = PruebasRepositoryEnMemoria()
        val vm = registrarVm(repo)

        vm.guardar()

        assertEquals("Ingresa el valor medido.", vm.uiState.value.errorMedicion)
        assertEquals(1, vm.uiState.value.intentosFallidos)
        assertTrue(repo.pruebas.isEmpty())
    }

    @Test
    fun registrar_medicionNoNumericaONegativa() {
        val vm = registrarVm()
        vm.guardar()

        vm.onMedicionChange("abc")
        assertEquals("La medición debe ser un número (ej: 125.5).", vm.uiState.value.errorMedicion)

        vm.onMedicionChange("-3")
        assertEquals("La medición no puede ser negativa.", vm.uiState.value.errorMedicion)

        vm.onMedicionChange("12,5")
        assertNull(vm.uiState.value.errorMedicion)
    }

    @Test
    fun registrar_fechaFuturaNoSeAcepta() {
        val vm = registrarVm()
        vm.onMedicionChange("100")
        vm.onFechaChange("24/10/2026")

        vm.guardar()

        assertEquals("La prueba no puede tener fecha u hora futura.", vm.uiState.value.errorFecha)
    }

    @Test
    fun registrar_rechazadaExigeObservaciones() {
        val repo = PruebasRepositoryEnMemoria()
        val vm = registrarVm(repo)
        vm.onMedicionChange("0.4")
        vm.onResultadoChange(OpcionesPrueba.RECHAZADO)

        vm.guardar()
        assertEquals("Describe el motivo del rechazo.", vm.uiState.value.errorObservaciones)
        assertTrue(repo.pruebas.isEmpty())

        vm.onObservacionesChange("Aislamiento bajo norma")
        vm.guardar()
        assertEquals(1, repo.pruebas.size)
    }

    @Test
    fun registrar_guardaNormalizadoConEvidenciaYPublicaElId() {
        val repo = PruebasRepositoryEnMemoria()
        val vm = registrarVm(repo, equipo = " RL-004 ")
        vm.onMedicionChange(" 12,5 ")
        vm.onFotoTomada("content://foto/1")
        vm.capturarUbicacion()

        vm.guardar()

        val guardada = repo.pruebas.single()
        assertEquals("RL-004", guardada.equipo)
        assertEquals("12.5", guardada.medicion)
        assertEquals("content://foto/1", guardada.fotoUri)
        assertEquals(-33.36, guardada.latitud!!, 0.0)
        assertEquals(guardada.id, vm.uiState.value.idGuardado)
        assertFalse(vm.uiState.value.guardando)

        vm.guardadoConsumido()
        assertNull(vm.uiState.value.idGuardado)
    }

    @Test
    fun registrar_errorDelRepositorioSeInforma() {
        val repoQueFalla = object : PruebasRepository by PruebasRepositoryEnMemoria() {
            override suspend fun insertar(prueba: Prueba): Long? = null
        }
        val vm = registrarVm(repoQueFalla)
        vm.onMedicionChange("100")

        vm.guardar()

        assertTrue(vm.uiState.value.errorAlGuardar)
        assertNull(vm.uiState.value.idGuardado)
    }

    @Test
    fun registrar_ubicacionInformaCadaEstado() {
        val conGps = registrarVm()
        conGps.capturarUbicacion()
        assertEquals(EstadoUbicacion.CAPTURADA, conGps.uiState.value.estadoUbicacion)
        assertNotNull(conGps.uiState.value.coordenadas)

        val sinGps = registrarVm(ubicacion = { ResultadoUbicacion.GpsDesactivado })
        sinGps.capturarUbicacion()
        assertEquals(EstadoUbicacion.GPS_DESACTIVADO, sinGps.uiState.value.estadoUbicacion)

        val sinPermiso = registrarVm()
        sinPermiso.onPermisoUbicacionDenegado()
        assertEquals(EstadoUbicacion.SIN_PERMISO, sinPermiso.uiState.value.estadoUbicacion)
    }

    @Test
    fun historial_sinPruebasUsaEquiposPorDefecto() {
        val vm = HistorialEquipoViewModel(PruebasRepositoryEnMemoria(), "TR-001")
        vm.cargar()

        assertEquals(CatalogoEquipos.CODIGOS_POR_DEFECTO, vm.uiState.value.equipos)
        assertTrue(vm.uiState.value.pruebas.isEmpty())
    }

    @Test
    fun historial_cargaPruebasDelEquipoMasRecientesPrimero() {
        val repo = PruebasRepositoryEnMemoria(
            listOf(prueba(1, "TR-001"), prueba(2, "RL-004"), prueba(3, "TR-001"))
        )
        val vm = HistorialEquipoViewModel(repo, "TR-001")
        vm.cargar()

        assertEquals(listOf(3L, 1L), vm.uiState.value.pruebas.map { it.id })
        assertEquals(listOf("RL-004", "TR-001"), vm.uiState.value.equipos)
    }

    @Test
    fun historial_seleccionarEquipoRecarga() {
        val repo = PruebasRepositoryEnMemoria(listOf(prueba(1, "TR-001"), prueba(2, "RL-004")))
        val vm = HistorialEquipoViewModel(repo, "TR-001")
        vm.cargar()

        vm.seleccionarEquipo("RL-004")

        assertEquals("RL-004", vm.uiState.value.equipoSeleccionado)
        assertEquals(listOf(2L), vm.uiState.value.pruebas.map { it.id })
    }

    @Test
    fun historial_muestraSoloLasUltimasCincoPeroCuentaTodas() {
        val repo = PruebasRepositoryEnMemoria(
            (1L..7L).map { prueba(it, "TR-001") } + prueba(8, "RL-004")
        )
        val vm = HistorialEquipoViewModel(repo, "TR-001")
        vm.cargar()

        assertEquals(listOf(7L, 6L, 5L, 4L, 3L), vm.uiState.value.pruebas.map { it.id })
        assertEquals(7, vm.uiState.value.totalPruebas)
        assertEquals(8, repo.pruebas.size)
    }

    @Test
    fun historial_reflejaPruebasNuevasAlRecargar() = runTest {
        val repo = PruebasRepositoryEnMemoria()
        val vm = HistorialEquipoViewModel(repo, "TR-001")
        vm.cargar()

        repo.insertar(prueba(equipo = "TR-001"))
        vm.cargar()

        assertEquals(1, vm.uiState.value.pruebas.size)
    }

    @Test
    fun detalle_encuentraLaPrueba() {
        val vm = DetallePruebaViewModel(PruebasRepositoryEnMemoria(listOf(prueba(7))), 7L)

        assertFalse(vm.uiState.value.cargando)
        assertEquals(7L, vm.uiState.value.prueba?.id)
        assertTrue(vm.textoRespaldo()!!.contains("Medición: 1200 MΩ"))
    }

    @Test
    fun detalle_pruebaInexistenteTerminaSinDatos() {
        val vm = DetallePruebaViewModel(PruebasRepositoryEnMemoria(), 99L)

        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.prueba)
        assertNull(vm.textoRespaldo())
    }

    @Test
    fun detalle_comparaConLaPruebaAnteriorDelMismoTipo() {
        val repo = PruebasRepositoryEnMemoria(
            listOf(prueba(1, medicion = "1000"), prueba(2, equipo = "RL-004"), prueba(3, medicion = "1200"))
        )
        val vm = DetallePruebaViewModel(repo, 3L)

        val comparacion = vm.uiState.value.comparacion!!
        assertEquals(1L, comparacion.anterior.id)
        assertEquals(200.0, comparacion.diferencia, 0.001)
        assertEquals(20.0, comparacion.porcentaje!!, 0.001)
    }

    @Test
    fun comparacion_sinAnteriorComparableEsNula() {
        val actual = prueba(2)
        val otroTipo = prueba(1).copy(tipoPrueba = "Prueba de continuidad")

        assertNull(compararConAnterior(actual, listOf(otroTipo, actual)))
    }
}
