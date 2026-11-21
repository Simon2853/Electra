package com.example.electra

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.example.electra.data.AppContainer
import com.example.electra.data.remote.ConfiguracionApi

class ElectraApplication : Application(), SingletonImageLoader.Factory {
    val contenedor by lazy { AppContainer(this) }

    /**
     * Coil descarga las fotos con el mismo cliente HTTP que Retrofit, así también llevan el encabezado
     * que omite la página de advertencia del túnel de VS Code.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { ConfiguracionApi.clienteHttp })) }
            .build()
}
