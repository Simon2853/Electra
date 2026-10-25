package com.example.electra.ui.viewmodel

import com.example.electra.data.ProtocolosRepositoryEnMemoria
import com.example.electra.data.PruebasRepositoryEnMemoria
import com.example.electra.model.CatalogoEquipos
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
        assertEquals(9, repo.protocolos.size)
    }

    private fun crearVm(
        protocolos: ProtocolosRepositoryEnMemoria = repoConSemilla(),
        codigoInicial: String = ""
    ) = CrearProtocoloViewModel(protocolos, PruebasRepositoryEnMemoria(), codigoInicial, fecha = "23/10/2026")

    @Test
    fun crear_sugiereElSiguienteNumeroYEquipo() {
        val vm = crearVm()

        assertEquals("#000126", vm.uiState.value.numero)
        assertEquals("TR-001", vm.uiState.value.equipo)
        assertEquals("23/10/2026", vm.uiState.value.fecha)
        assertEquals(CatalogoEquipos.CODIGOS, vm.uiState.value.equipos)
    }

    @Test
    fun siguienteNumero_noChocaConNumerosExistentes() {
        val lista = listOf(protocolo(1, "#000010"), protocolo(2, "#000200"))
        assertEquals("#000201", siguienteNumero(lista))
        assertEquals("#000001", siguienteNumero(emptyList()))
    }

    @Test
    fun crear_respetaElEquipoInicial() {
        val vm = crearVm(codigoInicial = "CE-008")
        assertEquals("CE-008", vm.uiState.value.equipo)
    }

    @Test
    fun crear_sinResponsableMarcaElCampoYNoGuarda() {
        val repo = repoConSemilla()
        val vm = crearVm(repo)
        assertNull(vm.uiState.value.errorResponsable)

        vm.guardar()

        assertEquals(ResultadoProtocolo.CAMPOS_INCOMPLETOS, vm.uiState.value.resultado)
        assertEquals("Ingresa el responsable de la inspección.", vm.uiState.value.errorResponsable)
        assertEquals(4, repo.protocolos.size)
    }

    @Test
    fun crear_numeroConFormatoInvalido() {
        val vm = crearVm()
        vm.onNumeroChange("12A")
        vm.onResponsableChange("Ana Test")

        vm.guardar()

        assertEquals("Usa 6 dígitos, por ejemplo #000130.", vm.uiState.value.errorNumero)
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
    fun crear_numeroDuplicadoSeMuestraEnElCampo() {
        val vm = crearVm()

        vm.onNumeroChange("000125")
        vm.onResponsableChange("Ana")
        vm.guardar()

        assertEquals(ResultadoProtocolo.DUPLICADO, vm.uiState.value.resultado)
        assertEquals("Ya existe un protocolo con ese número.", vm.uiState.value.errorNumero)
        vm.resultadoMostrado()
        assertNull(vm.uiState.value.resultado)

        vm.onNumeroChange("000300")
        assertNull(vm.uiState.value.errorNumero)
    }

    private fun detalleVm(numero: String, pruebas: PruebasRepositoryEnMemoria = PruebasRepositoryEnMemoria()) =
        DetalleProtocoloViewModel(repoConSemilla(), pruebas, numero)

    @Test
    fun detalle_cargaElProtocoloYLasPruebasDelEquipo() {
        val pruebas = PruebasRepositoryEnMemoria(listOf(prueba(1, "TR-001"), prueba(2, "RL-004")))
        val vm = detalleVm("000124", pruebas)
        assertTrue(vm.uiState.value.cargando)

        vm.cargar()

        assertFalse(vm.uiState.value.cargando)
        assertEquals("#000124", vm.uiState.value.protocolo?.numero)
        assertEquals(listOf(1L), vm.uiState.value.pruebas.map { it.id })
        assertTrue(vm.textoRespaldo()!!.contains("PROTOCOLO #000124"))
    }

    @Test
    fun detalle_protocoloInexistente() {
        val vm = detalleVm("999999")
        vm.cargar()

        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.protocolo)
    }

    @Test
    fun detalle_formalizarCambiaElEstado() {
        val vm = detalleVm("000124")
        vm.cargar()

        vm.formalizar()

        assertEquals("Formalizado", vm.uiState.value.protocolo?.estado)
        assertTrue(vm.uiState.value.formalizado)
        assertEquals(MensajeProtocolo.FORMALIZADO, vm.uiState.value.mensaje)
    }

    @Test
    fun detalle_editarObservacionDesdeElDialogo() {
        val vm = detalleVm("000124")
        vm.cargar()

        vm.abrirEdicion()
        assertTrue(vm.uiState.value.editandoObservacion)
        vm.onObservacionChange("  Revisar conexiones ")
        vm.guardarObservacion()

        assertEquals("Revisar conexiones", vm.uiState.value.protocolo?.observaciones)
        assertFalse(vm.uiState.value.editandoObservacion)
        assertEquals(MensajeProtocolo.OBSERVACION_ACTUALIZADA, vm.uiState.value.mensaje)

        vm.mensajeMostrado()
        assertNull(vm.uiState.value.mensaje)
    }

    @Test
    fun detalle_observacionDemasiadoLargaNoSeGuarda() {
        val vm = detalleVm("000124")
        vm.cargar()

        vm.abrirEdicion()
        vm.onObservacionChange("x".repeat(301))
        vm.guardarObservacion()

        assertEquals("Máximo 300 caracteres.", vm.uiState.value.errorObservacion)
        assertTrue(vm.uiState.value.editandoObservacion)
    }

    @Test
    fun detalle_actualizarObservacionesDeProtocoloInexistenteInformaError() {
        val vm = detalleVm("999999")

        vm.actualizarObservaciones("Nota")

        assertEquals(MensajeProtocolo.ERROR_ACTUALIZAR, vm.uiState.value.mensaje)
    }
}
