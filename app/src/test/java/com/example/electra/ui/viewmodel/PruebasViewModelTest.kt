package com.example.electra.ui.viewmodel

import com.example.electra.data.PruebasRepository
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.Prueba
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PruebasViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    // --- REGISTRAR PRUEBA ---

    @Test
    fun registrar_medicionVaciaNoGuarda() {
        val repo = PruebasRepositoryEnMemoria()
        val vm = RegistrarPruebaViewModel(repo)

        vm.guardar(prueba(medicion = "  "))

        assertEquals(ResultadoGuardado.MEDICION_VACIA, vm.uiState.value.resultado)
        assertTrue(repo.pruebas.isEmpty())
    }

    @Test
    fun registrar_guardaConCodigoDeEquipoRecortado() {
        val repo = PruebasRepositoryEnMemoria()
        val vm = RegistrarPruebaViewModel(repo)

        vm.guardar(prueba(equipo = " RL-004 "))

        assertEquals(ResultadoGuardado.EXITO, vm.uiState.value.resultado)
        assertEquals("RL-004", repo.pruebas.single().equipo)
        assertFalse(vm.uiState.value.guardando)

        vm.resultadoMostrado()
        assertNull(vm.uiState.value.resultado)
    }

    @Test
    fun registrar_errorDelRepositorioSeInforma() {
        val repoQueFalla = object : PruebasRepository by PruebasRepositoryEnMemoria() {
            override suspend fun insertar(prueba: Prueba): Long? = null
        }
        val vm = RegistrarPruebaViewModel(repoQueFalla)

        vm.guardar(prueba())

        assertEquals(ResultadoGuardado.ERROR, vm.uiState.value.resultado)
    }

    // --- HISTORIAL ---

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
        // No se borra nada de la base
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

    // --- DETALLE ---

    @Test
    fun detalle_encuentraLaPrueba() {
        val vm = DetallePruebaViewModel(PruebasRepositoryEnMemoria(listOf(prueba(7))), 7L)

        assertFalse(vm.uiState.value.cargando)
        assertEquals(7L, vm.uiState.value.prueba?.id)
    }

    @Test
    fun detalle_pruebaInexistenteTerminaSinDatos() {
        val vm = DetallePruebaViewModel(PruebasRepositoryEnMemoria(), 99L)

        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.prueba)
    }
}
