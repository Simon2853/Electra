package com.example.electra.ui.viewmodel

import com.example.electra.data.ProtocolosRepositoryEnMemoria
import com.example.electra.data.PruebasRepositoryEnMemoria
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProtocolosViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun repoConSemilla() = ProtocolosRepositoryEnMemoria(
        listOf(
            protocolo(1, "#000122"),
            protocolo(2, "#000123", "Formalizado"),
            protocolo(3, "#000124"),
            protocolo(4, "#000125", "Formalizado")
        )
    )

    // --- LISTA ---

    @Test
    fun lista_cargaLosProtocolosMasRecientesPrimero() {
        val vm = ProtocolosViewModel(repoConSemilla())
        assertTrue(vm.protocolos.value.isEmpty())

        vm.cargar()

        assertEquals(listOf("#000125", "#000124", "#000123", "#000122"), vm.protocolos.value.map { it.numero })
    }

    @Test
    fun lista_muestraSoloLosUltimosCincoDeCadaEquipo() {
        val tr001 = (1L..7L).map { protocolo(it, "#%06d".format(it)) }
        val rl004 = listOf(
            protocolo(8, "#000008").copy(equipo = "RL-004"),
            protocolo(9, "#000009").copy(equipo = "RL-004")
        )
        val repo = ProtocolosRepositoryEnMemoria(tr001 + rl004)
        val vm = ProtocolosViewModel(repo)

        vm.cargar()

        assertEquals(
            listOf("#000009", "#000008", "#000007", "#000006", "#000005", "#000004", "#000003"),
            vm.protocolos.value.map { it.numero }
        )
        // No se borra nada de la base
        assertEquals(9, repo.protocolos.size)
    }

    // --- CREAR ---

    private fun crearVm(
        protocolos: ProtocolosRepositoryEnMemoria = repoConSemilla(),
        codigoInicial: String = ""
    ) = CrearProtocoloViewModel(protocolos, PruebasRepositoryEnMemoria(), codigoInicial, fecha = "23/10/2026")

    @Test
    fun crear_sugiereNumeroYEquipo() {
        val vm = crearVm()

        assertEquals("#000130", vm.uiState.value.numero)
        assertEquals("TR-001", vm.uiState.value.equipo)
        assertEquals("23/10/2026", vm.uiState.value.fecha)
        assertEquals(4, vm.uiState.value.equipos.size)
    }

    @Test
    fun crear_respetaElEquipoInicial() {
        val vm = crearVm(codigoInicial = "CE-008")
        assertEquals("CE-008", vm.uiState.value.equipo)
    }

    @Test
    fun crear_sinResponsableNoGuarda() {
        val repo = repoConSemilla()
        val vm = crearVm(repo)

        vm.guardar()

        assertEquals(ResultadoProtocolo.CAMPOS_INCOMPLETOS, vm.uiState.value.resultado)
        assertEquals(4, repo.protocolos.size)
    }

    @Test
    fun crear_guardaConDatosNormalizados() {
        val repo = repoConSemilla()
        val vm = crearVm(repo)

        vm.onNumeroChange(" 000200 ")
        vm.onEquipoChange("RL-004")
        vm.onEstadoChange("Formalizado")
        vm.onResponsableChange("  Ana Test ")
        vm.onObservacionesChange(" Nota ")
        vm.guardar()

        assertEquals(ResultadoProtocolo.EXITO, vm.uiState.value.resultado)
        val guardado = repo.protocolos.last()
        assertEquals("#000200", guardado.numero)
        assertEquals("RL-004", guardado.equipo)
        assertEquals("Formalizado", guardado.estado)
        assertEquals("Ana Test", guardado.responsable)
        assertEquals("Nota", guardado.observaciones)
        assertEquals("23/10/2026", guardado.fecha)
    }

    @Test
    fun crear_numeroDuplicadoSeInforma() {
        val vm = crearVm()

        vm.onNumeroChange("000125")
        vm.onResponsableChange("Ana")
        vm.guardar()

        assertEquals(ResultadoProtocolo.DUPLICADO, vm.uiState.value.resultado)
        vm.resultadoMostrado()
        assertNull(vm.uiState.value.resultado)
    }

    // --- DETALLE ---

    @Test
    fun detalle_cargaElProtocolo() {
        val vm = DetalleProtocoloViewModel(repoConSemilla(), "000124")
        assertTrue(vm.uiState.value.cargando)

        vm.cargar()

        assertFalse(vm.uiState.value.cargando)
        assertEquals("#000124", vm.uiState.value.protocolo?.numero)
    }

    @Test
    fun detalle_protocoloInexistente() {
        val vm = DetalleProtocoloViewModel(repoConSemilla(), "999999")
        vm.cargar()

        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.protocolo)
    }

    @Test
    fun detalle_formalizarCambiaElEstado() {
        val vm = DetalleProtocoloViewModel(repoConSemilla(), "000124")
        vm.cargar()

        vm.formalizar()

        assertEquals("Formalizado", vm.uiState.value.protocolo?.estado)
    }

    @Test
    fun detalle_actualizarObservaciones() {
        val vm = DetalleProtocoloViewModel(repoConSemilla(), "000124")
        vm.cargar()

        vm.actualizarObservaciones("  Revisar conexiones ")

        assertEquals("Revisar conexiones", vm.uiState.value.protocolo?.observaciones)
        assertEquals(MensajeProtocolo.OBSERVACION_ACTUALIZADA, vm.uiState.value.mensaje)

        vm.mensajeMostrado()
        assertNull(vm.uiState.value.mensaje)
    }

    @Test
    fun detalle_actualizarObservacionesDeProtocoloInexistenteInformaError() {
        val vm = DetalleProtocoloViewModel(repoConSemilla(), "999999")

        vm.actualizarObservaciones("Nota")

        assertEquals(MensajeProtocolo.ERROR_ACTUALIZAR, vm.uiState.value.mensaje)
    }
}
