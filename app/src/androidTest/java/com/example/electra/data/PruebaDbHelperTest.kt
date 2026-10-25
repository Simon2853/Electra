package com.example.electra.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.electra.contextoDePrueba
import com.example.electra.limpiarAlmacenamiento
import com.example.electra.model.Protocolo
import com.example.electra.model.Prueba
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PruebaDbHelperTest {

    private lateinit var contexto: Context
    private lateinit var db: PruebaDbHelper

    @Before
    fun setUp() {
        contexto = contextoDePrueba()
        limpiarAlmacenamiento(contexto)
        db = PruebaDbHelper(contexto)
    }

    @After
    fun tearDown() {
        db.close()
        limpiarAlmacenamiento(contexto)
    }

    private fun prueba(equipo: String = "TR-001", medicion: String = "1200") = Prueba(
        equipo = equipo,
        tipoPrueba = "Resistencia de aislamiento",
        fecha = "23/10/2026",
        hora = "10:30",
        medicion = medicion,
        unidad = "MΩ",
        resultado = "Aprobado",
        observaciones = "Sin novedades"
    )

    @Test
    fun insertarPrueba_yBuscarPorId_devuelveLosMismosDatos() {
        val id = db.insertarPrueba(prueba())
        assertNotEquals(-1L, id)

        val guardada = db.buscarPruebaPorId(id)
        assertEquals(prueba().copy(id = id), guardada)
    }

    @Test
    fun listarPruebas_ordenaDeLaMasRecienteALaMasAntigua() {
        val primera = db.insertarPrueba(prueba(medicion = "1"))
        val segunda = db.insertarPrueba(prueba(medicion = "2"))

        assertEquals(listOf(segunda, primera), db.listarPruebas().map { it.id })
    }

    @Test
    fun listarPruebasPorEquipo_filtraEIgnoraEspacios() {
        db.insertarPrueba(prueba(equipo = "TR-001"))
        db.insertarPrueba(prueba(equipo = " TR-001 "))
        db.insertarPrueba(prueba(equipo = "RL-004"))

        assertEquals(2, db.listarPruebasPorEquipo("  TR-001").size)
        assertEquals(1, db.listarPruebasPorEquipo("RL-004").size)
        assertTrue(db.listarPruebasPorEquipo("CE-008").isEmpty())
    }

    @Test
    fun eliminarPrueba_laQuitaDeLaBase() {
        val id = db.insertarPrueba(prueba())

        assertEquals(1, db.eliminarPrueba(id))
        assertNull(db.buscarPruebaPorId(id))
        assertEquals(0, db.eliminarPrueba(id))
    }

    @Test
    fun obtenerEquiposConRegistros_sinPruebasDevuelveListaPorDefecto() {
        assertEquals(listOf("TR-001", "TR-002", "RL-004", "CE-008"), db.obtenerEquiposConRegistros())
    }

    @Test
    fun obtenerEquiposConRegistros_devuelveEquiposDistintosOrdenados() {
        db.insertarPrueba(prueba(equipo = "RL-004"))
        db.insertarPrueba(prueba(equipo = "CE-008"))
        db.insertarPrueba(prueba(equipo = "RL-004"))

        assertEquals(listOf("CE-008", "RL-004"), db.obtenerEquiposConRegistros())
    }

    @Test
    fun baseNueva_incluyeLosCuatroProtocolosSemilla() {
        val numeros = db.listarProtocolos().map { it.numero }
        assertEquals(listOf("#000122", "#000123", "#000124", "#000125"), numeros.sorted())
    }

    @Test
    fun insertarProtocolo_agregaElNumeralYRecortaEspacios() {
        val id = db.insertarProtocolo(
            Protocolo(
                numero = " 000200 ",
                equipo = " TR-001 ",
                fecha = "23/10/2026",
                estado = "Registrado en terreno",
                responsable = " Ana Test ",
                observaciones = " Nota "
            )
        )
        assertNotEquals(-1L, id)

        val guardado = db.buscarProtocoloPorNumero("000200")
        assertNotNull(guardado)
        assertEquals("#000200", guardado!!.numero)
        assertEquals("TR-001", guardado.equipo)
        assertEquals("Ana Test", guardado.responsable)
        assertEquals("Nota", guardado.observaciones)
    }

    @Test
    fun insertarProtocolo_conNumeroDuplicadoDevuelveError() {
        val duplicado = Protocolo(
            numero = "000125",
            equipo = "TR-001",
            fecha = "23/10/2026",
            estado = "Formalizado"
        )
        assertEquals(-1L, db.insertarProtocolo(duplicado))
    }

    @Test
    fun buscarProtocoloPorNumero_aceptaConYSinNumeral() {
        assertEquals("#000125", db.buscarProtocoloPorNumero("000125")?.numero)
        assertEquals("#000125", db.buscarProtocoloPorNumero("#000125")?.numero)
        assertNull(db.buscarProtocoloPorNumero("999999"))
    }

    @Test
    fun cambiarEstadoProtocolo_formalizaUnProtocoloEnTerreno() {
        assertEquals("Registrado en terreno", db.buscarProtocoloPorNumero("000124")?.estado)

        assertTrue(db.cambiarEstadoProtocolo("000124", "Formalizado"))
        assertEquals("Formalizado", db.buscarProtocoloPorNumero("000124")?.estado)
    }

    @Test
    fun cambiarEstadoProtocolo_inexistenteDevuelveFalse() {
        assertFalse(db.cambiarEstadoProtocolo("999999", "Formalizado"))
    }

    @Test
    fun actualizarObservacionesProtocolo_guardaElTextoRecortado() {
        assertTrue(db.actualizarObservacionesProtocolo("#000122", "  Revisar conexiones  "))
        assertEquals("Revisar conexiones", db.buscarProtocoloPorNumero("000122")?.observaciones)
    }

    @Test
    fun onUpgrade_deVersion1CreaTablaProtocolosConSemilla() {
        db.close()
        contexto.deleteDatabase("electra.db")

        val ruta = contexto.getDatabasePath("electra.db").apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(ruta, null).use { v1 ->
            v1.execSQL(
                "CREATE TABLE pruebas (id INTEGER PRIMARY KEY AUTOINCREMENT, equipo TEXT NOT NULL, " +
                        "tipo_prueba TEXT NOT NULL, fecha TEXT NOT NULL, hora TEXT NOT NULL, " +
                        "medicion TEXT NOT NULL, unidad TEXT NOT NULL, resultado TEXT NOT NULL, " +
                        "observaciones TEXT NOT NULL)"
            )
            v1.execSQL(
                "INSERT INTO pruebas (equipo, tipo_prueba, fecha, hora, medicion, unidad, resultado, observaciones) " +
                        "VALUES ('TR-001', 'Rigidez', '01/01/2026', '08:00', '50', 'kV', 'Aprobado', '')"
            )
            v1.version = 1
        }

        db = PruebaDbHelper(contexto)

        assertEquals(1, db.listarPruebas().size)
        assertEquals(4, db.listarProtocolos().size)
    }

    @Test
    fun onUpgrade_deVersion2AgregaEvidenciaYCorrigeCodigos() {
        db.close()
        contexto.deleteDatabase("electra.db")

        val ruta = contexto.getDatabasePath("electra.db").apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(ruta, null).use { v2 ->
            v2.execSQL(
                "CREATE TABLE pruebas (id INTEGER PRIMARY KEY AUTOINCREMENT, equipo TEXT NOT NULL, " +
                        "tipo_prueba TEXT NOT NULL, fecha TEXT NOT NULL, hora TEXT NOT NULL, " +
                        "medicion TEXT NOT NULL, unidad TEXT NOT NULL, resultado TEXT NOT NULL, " +
                        "observaciones TEXT NOT NULL)"
            )
            v2.execSQL(
                "CREATE TABLE protocolos (id INTEGER PRIMARY KEY AUTOINCREMENT, numero TEXT UNIQUE NOT NULL, " +
                        "equipo TEXT NOT NULL, fecha TEXT NOT NULL, estado TEXT NOT NULL, " +
                        "responsable TEXT NOT NULL, observaciones TEXT)"
            )
            v2.execSQL(
                "INSERT INTO protocolos (numero, equipo, fecha, estado, responsable) " +
                        "VALUES ('#000125', 'TRA-001', '02/10/2026', 'Formalizado', 'Juan Pérez')"
            )
            v2.execSQL(
                "INSERT INTO pruebas (equipo, tipo_prueba, fecha, hora, medicion, unidad, resultado, observaciones) " +
                        "VALUES ('TR-001', 'Rigidez', '01/01/2026', '08:00', '50', 'kV', 'Aprobado', '')"
            )
            v2.version = 2
        }

        db = PruebaDbHelper(contexto)

        assertEquals("TR-001", db.buscarProtocoloPorNumero("000125")?.equipo)
        val antigua = db.listarPruebas().single()
        assertNull(antigua.fotoUri)
        assertNull(antigua.latitud)

        val id = db.insertarPrueba(prueba().copy(fotoUri = "content://foto/1", latitud = -33.4, longitud = -70.6))
        val nueva = db.buscarPruebaPorId(id)
        assertEquals("content://foto/1", nueva?.fotoUri)
        assertEquals(-33.4, nueva?.latitud!!, 0.0001)
        assertEquals(-70.6, nueva.longitud!!, 0.0001)
    }
}
