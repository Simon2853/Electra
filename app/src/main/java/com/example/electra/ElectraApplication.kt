package com.example.electra

import android.app.Application
import com.example.electra.data.AppContainer

class ElectraApplication : Application() {
    val contenedor by lazy { AppContainer(this) }
}
