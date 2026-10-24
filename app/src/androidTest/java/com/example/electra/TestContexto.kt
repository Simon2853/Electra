package com.example.electra

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

private const val PREFIJO = "test_"

/**
 * Contexto de la app con almacenamiento aislado: conserva los recursos reales (logo, strings),
 * pero la base de datos y las SharedPreferences se guardan con prefijo "test_", así los tests
 * nunca leen ni modifican los datos reales del dispositivo.
 */
class ContextoAislado(base: Context) : ContextWrapper(base) {

    override fun getApplicationContext(): Context = this

    override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
        super.getSharedPreferences(PREFIJO + name, mode)

    override fun getDatabasePath(name: String): File =
        super.getDatabasePath(PREFIJO + name)

    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?
    ): SQLiteDatabase = super.openOrCreateDatabase(PREFIJO + name, mode, factory)

    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?,
        errorHandler: DatabaseErrorHandler?
    ): SQLiteDatabase = super.openOrCreateDatabase(PREFIJO + name, mode, factory, errorHandler)

    override fun deleteDatabase(name: String): Boolean =
        super.deleteDatabase(PREFIJO + name)
}

fun contextoDePrueba(): Context =
    ContextoAislado(InstrumentationRegistry.getInstrumentation().targetContext)

fun limpiarAlmacenamiento(contexto: Context) {
    contexto.deleteDatabase("electra.db")
    contexto.getSharedPreferences("electra_usuarios", Context.MODE_PRIVATE)
        .edit().clear().commit()
}
