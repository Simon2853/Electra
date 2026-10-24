package com.example.electra.data

import android.content.Context

/** Inyección manual de dependencias: una sola base de datos compartida por todos los repositorios. */
class AppContainer(context: Context) {

    private val contexto = context.applicationContext

    private val db by lazy { PruebaDbHelper(contexto) }

    val pruebas: PruebasRepository by lazy { SqlitePruebasRepository(db) }
    val protocolos: ProtocolosRepository by lazy { SqliteProtocolosRepository(db) }
    val usuarios: UsuariosRepository by lazy { LocalUsuariosRepository(contexto) }
}
