package com.example.electra.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.electra.model.CatalogoEquipos
import com.example.electra.model.Protocolo
import com.example.electra.model.Prueba

class PruebaDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "electra.db"
        private const val VERSION_BD = 5

        /** Login del usuario que creó el registro (misma columna en pruebas y protocolos). */
        private const val CREADO_POR = "creado_por"

        /** Momento en que se guardó el registro (epoch ms; misma columna en ambas tablas). */
        private const val CREADO_EN = "creado_en"

        private const val TABLA_PRUEBAS = "pruebas"

        private const val ID = "id"
        private const val EQUIPO = "equipo"
        private const val TIPO_PRUEBA = "tipo_prueba"
        private const val FECHA = "fecha"
        private const val HORA = "hora"
        private const val MEDICION = "medicion"
        private const val UNIDAD = "unidad"
        private const val RESULTADO = "resultado"
        private const val OBSERVACIONES = "observaciones"
        private const val FOTO_URI = "foto_uri"
        private const val LATITUD = "latitud"
        private const val LONGITUD = "longitud"

        private const val SQL_CREAR_TABLA_PRUEBAS =
            "CREATE TABLE $TABLA_PRUEBAS (" +
                    "$ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$EQUIPO TEXT NOT NULL, " +
                    "$TIPO_PRUEBA TEXT NOT NULL, " +
                    "$FECHA TEXT NOT NULL, " +
                    "$HORA TEXT NOT NULL, " +
                    "$MEDICION TEXT NOT NULL, " +
                    "$UNIDAD TEXT NOT NULL, " +
                    "$RESULTADO TEXT NOT NULL, " +
                    "$OBSERVACIONES TEXT NOT NULL, " +
                    "$FOTO_URI TEXT, " +
                    "$LATITUD REAL, " +
                    "$LONGITUD REAL, " +
                    "$CREADO_POR TEXT, " +
                    "$CREADO_EN INTEGER)"

        private const val TABLA_PROTOCOLOS = "protocolos"

        private const val PROTOCOLO_ID = "id"
        private const val PROTOCOLO_NUMERO = "numero"
        private const val PROTOCOLO_EQUIPO = "equipo"
        private const val PROTOCOLO_FECHA = "fecha"
        private const val PROTOCOLO_ESTADO = "estado"
        private const val PROTOCOLO_RESPONSABLE = "responsable"
        private const val PROTOCOLO_OBSERVACIONES = "observaciones"

        private val CODIGOS_ANTIGUOS =
            mapOf("TRA-001" to "TR-001", "REL-025" to "RL-004", "TRA-002" to "TR-002")

        private const val SQL_CREAR_TABLA_PROTOCOLOS =
            "CREATE TABLE $TABLA_PROTOCOLOS (" +
                    "$PROTOCOLO_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$PROTOCOLO_NUMERO TEXT UNIQUE NOT NULL, " +
                    "$PROTOCOLO_EQUIPO TEXT NOT NULL, " +
                    "$PROTOCOLO_FECHA TEXT NOT NULL, " +
                    "$PROTOCOLO_ESTADO TEXT NOT NULL, " +
                    "$PROTOCOLO_RESPONSABLE TEXT NOT NULL, " +
                    "$PROTOCOLO_OBSERVACIONES TEXT, " +
                    "$CREADO_POR TEXT, " +
                    "$CREADO_EN INTEGER)"

        /**
         * Protocolos de ejemplo que versiones anteriores insertaban al crear la base. Se eliminan al
         * actualizar a la versión 4: no pertenecen a ningún usuario y una cuenta nueva debe partir vacía.
         * Se identifican por número + fecha + responsable (estado y observaciones pudieron cambiar).
         */
        private val PROTOCOLOS_DE_EJEMPLO = listOf(
            Triple("#000125", "02/10/2026", "Juan Pérez"),
            Triple("#000124", "01/10/2026", "Carlos Soto"),
            Triple("#000123", "28/09/2026", "Juan Pérez"),
            Triple("#000122", "25/09/2026", "Diego Araya")
        )
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_TABLA_PRUEBAS)
        db.execSQL(SQL_CREAR_TABLA_PROTOCOLOS)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            db.execSQL(SQL_CREAR_TABLA_PROTOCOLOS)
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLA_PRUEBAS ADD COLUMN $FOTO_URI TEXT")
            db.execSQL("ALTER TABLE $TABLA_PRUEBAS ADD COLUMN $LATITUD REAL")
            db.execSQL("ALTER TABLE $TABLA_PRUEBAS ADD COLUMN $LONGITUD REAL")

            CODIGOS_ANTIGUOS.forEach { (antiguo, nuevo) ->
                db.execSQL(
                    "UPDATE $TABLA_PROTOCOLOS SET $PROTOCOLO_EQUIPO = ? WHERE $PROTOCOLO_EQUIPO = ?",
                    arrayOf(nuevo, antiguo)
                )
            }
        }
        if (oldVersion < 4) {
            // Si la tabla se creó en este mismo upgrade (desde la v1) ya trae la columna.
            agregarColumnaSiFalta(db, TABLA_PRUEBAS, CREADO_POR, "TEXT")
            agregarColumnaSiFalta(db, TABLA_PROTOCOLOS, CREADO_POR, "TEXT")

            PROTOCOLOS_DE_EJEMPLO.forEach { (numero, fecha, responsable) ->
                db.delete(
                    TABLA_PROTOCOLOS,
                    "$PROTOCOLO_NUMERO = ? AND $PROTOCOLO_FECHA = ? AND $PROTOCOLO_RESPONSABLE = ? AND $CREADO_POR IS NULL",
                    arrayOf(numero, fecha, responsable)
                )
            }
        }
        if (oldVersion < 5) {
            agregarColumnaSiFalta(db, TABLA_PRUEBAS, CREADO_EN, "INTEGER")
            agregarColumnaSiFalta(db, TABLA_PROTOCOLOS, CREADO_EN, "INTEGER")
        }
    }

    private fun agregarColumnaSiFalta(db: SQLiteDatabase, tabla: String, columna: String, tipo: String) {
        val existe = db.rawQuery("PRAGMA table_info($tabla)", null).use { cursor ->
            val indiceNombre = cursor.getColumnIndexOrThrow("name")
            generateSequence { if (cursor.moveToNext()) cursor.getString(indiceNombre) else null }
                .any { it == columna }
        }
        if (!existe) db.execSQL("ALTER TABLE $tabla ADD COLUMN $columna $tipo")
    }

    /**
     * Cláusula WHERE para limitar los registros visibles (combinable con AND).
     * [creador]: solo los de ese usuario; [desde]: solo los guardados desde ese momento (epoch ms).
     * null = sin ese filtro.
     */
    private fun filtroVisibles(creador: String?, desde: Long?): Pair<String, Array<String>> {
        val condiciones = mutableListOf("1 = 1")
        val args = mutableListOf<String>()
        if (creador != null) {
            condiciones += "$CREADO_POR = ?"
            args += creador
        }
        if (desde != null) {
            condiciones += "$CREADO_EN >= ?"
            args += desde.toString()
        }
        return condiciones.joinToString(" AND ") to args.toTypedArray()
    }

    fun insertarPrueba(prueba: Prueba): Long {
        return try {
            writableDatabase.insertOrThrow(
                TABLA_PRUEBAS,
                null,
                valoresDe(prueba)
            )
        } catch (e: Exception) {
            android.util.Log.e("PruebaDbHelper", "Error al guardar la prueba", e)
            -1L
        }
    }

    /** [creador] null = pruebas de todos los usuarios. */
    fun listarPruebas(creador: String? = null, desde: Long? = null): List<Prueba> {
        val (filtro, args) = filtroVisibles(creador, desde)
        return consultarPruebas(
            "SELECT * FROM $TABLA_PRUEBAS WHERE $filtro ORDER BY $ID DESC",
            args
        )
    }

    /** [creador] null = pruebas de todos los usuarios. */
    fun listarPruebasPorEquipo(codigoEquipo: String, creador: String? = null, desde: Long? = null): List<Prueba> {
        val (filtro, args) = filtroVisibles(creador, desde)
        return consultarPruebas(
            "SELECT * FROM $TABLA_PRUEBAS WHERE TRIM($EQUIPO) = TRIM(?) AND $filtro ORDER BY $ID DESC",
            arrayOf(codigoEquipo.trim()) + args
        )
    }

    fun buscarPruebaPorId(id: Long): Prueba? {
        return consultarPruebas(
            "SELECT * FROM $TABLA_PRUEBAS WHERE $ID = ?",
            arrayOf(id.toString())
        ).firstOrNull()
    }

    fun eliminarPrueba(id: Long): Int {
        return writableDatabase.delete(
            TABLA_PRUEBAS,
            "$ID = ?",
            arrayOf(id.toString())
        )
    }

    /** [creador] null = equipos con pruebas de cualquier usuario. */
    fun obtenerEquiposConRegistros(creador: String? = null, desde: Long? = null): List<String> {
        val equipos = mutableListOf<String>()
        val (filtro, args) = filtroVisibles(creador, desde)
        val cursor = readableDatabase.rawQuery(
            "SELECT DISTINCT $EQUIPO FROM $TABLA_PRUEBAS WHERE $filtro ORDER BY $EQUIPO ASC",
            args
        )
        cursor.use {
            while (it.moveToNext()) {
                equipos.add(it.getString(0))
            }
        }
        return equipos.ifEmpty { CatalogoEquipos.CODIGOS_POR_DEFECTO }
    }

    fun actualizarObservacionesProtocolo(numero: String, nuevasObservaciones: String): Boolean {
        val numLimpio = numero.trim().replace("#", "")
        val numConHashtag = "#$numLimpio"

        val values = ContentValues().apply {
            put(PROTOCOLO_OBSERVACIONES, nuevasObservaciones.trim())
        }
        return writableDatabase.update(
            TABLA_PROTOCOLOS,
            values,
            "$PROTOCOLO_NUMERO = ? OR $PROTOCOLO_NUMERO = ?",
            arrayOf(numConHashtag, numLimpio)
        ) > 0
    }

    fun insertarProtocolo(protocolo: Protocolo): Long {
        return try {
            val numLimpio = protocolo.numero.trim()
            val numConHashtag = if (numLimpio.startsWith("#")) numLimpio else "#$numLimpio"

            val values = ContentValues().apply {
                put(PROTOCOLO_NUMERO, numConHashtag)
                put(PROTOCOLO_EQUIPO, protocolo.equipo.trim())
                put(PROTOCOLO_FECHA, protocolo.fecha.trim())
                put(PROTOCOLO_ESTADO, protocolo.estado.trim())
                put(PROTOCOLO_RESPONSABLE, protocolo.responsable.trim())
                put(PROTOCOLO_OBSERVACIONES, protocolo.observaciones.trim())
                put(CREADO_POR, protocolo.creadoPor?.trim()?.ifBlank { null })
                put(CREADO_EN, protocolo.creadoEn)
            }
            writableDatabase.insertOrThrow(TABLA_PROTOCOLOS, null, values)
        } catch (e: Exception) {
            android.util.Log.e("PruebaDbHelper", "Error al guardar protocolo", e)
            -1L
        }
    }

    /** [creador] null = protocolos de todos los usuarios. */
    fun listarProtocolos(creador: String? = null, desde: Long? = null): List<Protocolo> {
        val lista = mutableListOf<Protocolo>()
        val (filtro, args) = filtroVisibles(creador, desde)
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM $TABLA_PROTOCOLOS WHERE $filtro ORDER BY $PROTOCOLO_ID DESC",
            args
        )
        cursor.use {
            while (it.moveToNext()) {
                lista.add(leerProtocolo(it))
            }
        }
        return lista
    }

    fun buscarProtocoloPorNumero(numero: String): Protocolo? {
        val numLimpio = numero.trim().replace("#", "")
        val numConHashtag = "#$numLimpio"

        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM $TABLA_PROTOCOLOS WHERE $PROTOCOLO_NUMERO = ? OR $PROTOCOLO_NUMERO = ?",
            arrayOf(numConHashtag, numLimpio)
        )
        cursor.use {
            if (it.moveToFirst()) {
                return leerProtocolo(it)
            }
        }
        return null
    }

    fun cambiarEstadoProtocolo(numero: String, nuevoEstado: String): Boolean {
        val numLimpio = numero.trim().replace("#", "")
        val numConHashtag = "#$numLimpio"

        val values = ContentValues().apply {
            put(PROTOCOLO_ESTADO, nuevoEstado.trim())
        }
        return writableDatabase.update(
            TABLA_PROTOCOLOS,
            values,
            "$PROTOCOLO_NUMERO = ? OR $PROTOCOLO_NUMERO = ?",
            arrayOf(numConHashtag, numLimpio)
        ) > 0
    }

    private fun valoresDe(prueba: Prueba): ContentValues {
        return ContentValues().apply {
            put(EQUIPO, prueba.equipo)
            put(TIPO_PRUEBA, prueba.tipoPrueba)
            put(FECHA, prueba.fecha)
            put(HORA, prueba.hora)
            put(MEDICION, prueba.medicion)
            put(UNIDAD, prueba.unidad)
            put(RESULTADO, prueba.resultado)
            put(OBSERVACIONES, prueba.observaciones)
            put(FOTO_URI, prueba.fotoUri)
            put(LATITUD, prueba.latitud)
            put(LONGITUD, prueba.longitud)
            put(CREADO_POR, prueba.creadoPor?.trim()?.ifBlank { null })
            put(CREADO_EN, prueba.creadoEn)
        }
    }

    private fun consultarPruebas(
        sql: String,
        argumentos: Array<String>?
    ): List<Prueba> {
        val lista = mutableListOf<Prueba>()
        readableDatabase.rawQuery(sql, argumentos).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leerPrueba(cursor))
            }
        }
        return lista
    }

    private fun leerPrueba(cursor: Cursor): Prueba {
        return Prueba(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(ID)),
            equipo = cursor.getString(cursor.getColumnIndexOrThrow(EQUIPO)),
            tipoPrueba = cursor.getString(cursor.getColumnIndexOrThrow(TIPO_PRUEBA)),
            fecha = cursor.getString(cursor.getColumnIndexOrThrow(FECHA)),
            hora = cursor.getString(cursor.getColumnIndexOrThrow(HORA)),
            medicion = cursor.getString(cursor.getColumnIndexOrThrow(MEDICION)),
            unidad = cursor.getString(cursor.getColumnIndexOrThrow(UNIDAD)),
            resultado = cursor.getString(cursor.getColumnIndexOrThrow(RESULTADO)),
            observaciones = cursor.getString(cursor.getColumnIndexOrThrow(OBSERVACIONES)),
            fotoUri = cursor.textoONulo(FOTO_URI),
            latitud = cursor.decimalONulo(LATITUD),
            longitud = cursor.decimalONulo(LONGITUD),
            creadoPor = cursor.textoONulo(CREADO_POR),
            creadoEn = cursor.enteroONulo(CREADO_EN)
        )
    }

    private fun Cursor.textoONulo(columna: String): String? {
        val indice = getColumnIndex(columna)
        return if (indice == -1 || isNull(indice)) null else getString(indice)
    }

    private fun Cursor.decimalONulo(columna: String): Double? {
        val indice = getColumnIndex(columna)
        return if (indice == -1 || isNull(indice)) null else getDouble(indice)
    }

    private fun Cursor.enteroONulo(columna: String): Long? {
        val indice = getColumnIndex(columna)
        return if (indice == -1 || isNull(indice)) null else getLong(indice)
    }

    private fun leerProtocolo(cursor: Cursor): Protocolo {
        val idxObservaciones = cursor.getColumnIndex(PROTOCOLO_OBSERVACIONES)
        val observacionesTexto = if (idxObservaciones != -1 && !cursor.isNull(idxObservaciones)) {
            cursor.getString(idxObservaciones)
        } else {
            ""
        }

        return Protocolo(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(PROTOCOLO_ID)),
            numero = cursor.getString(cursor.getColumnIndexOrThrow(PROTOCOLO_NUMERO)),
            equipo = cursor.getString(cursor.getColumnIndexOrThrow(PROTOCOLO_EQUIPO)),
            fecha = cursor.getString(cursor.getColumnIndexOrThrow(PROTOCOLO_FECHA)),
            estado = cursor.getString(cursor.getColumnIndexOrThrow(PROTOCOLO_ESTADO)),
            responsable = cursor.getString(cursor.getColumnIndexOrThrow(PROTOCOLO_RESPONSABLE)),
            observaciones = observacionesTexto,
            creadoPor = cursor.textoONulo(CREADO_POR),
            creadoEn = cursor.enteroONulo(CREADO_EN)
        )
    }
}
