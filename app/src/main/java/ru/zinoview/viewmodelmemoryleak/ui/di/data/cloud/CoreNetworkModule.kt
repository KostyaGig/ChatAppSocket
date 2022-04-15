package ru.zinoview.viewmodelmemoryleak.ui.di.data.cloud

import com.google.gson.Gson
import io.socket.client.IO
import org.koin.dsl.module.module
import ru.zinoview.viewmodelmemoryleak.data.core.cloud.ActivityConnection
import ru.zinoview.viewmodelmemoryleak.data.core.cloud.Json
import ru.zinoview.viewmodelmemoryleak.data.core.cloud.SocketConnection
import ru.zinoview.viewmodelmemoryleak.ui.di.core.Module

class CoreNetworkModule : Module {

    private val networkModule = module {
        single<io.socket.client.Socket> {
            IO.socket(URI)
        }

        single<SocketConnection> {
            SocketConnection.Base(
                ActivityConnection.Base()
            )
        }

        single<Json> {
            Json.Base()
        }

        single<Gson> {
            Gson()
        }
    }

    override fun add(modules: MutableList<org.koin.dsl.module.Module>) {
        modules.add(networkModule)
    }

    private companion object {
        private const val URI = "http://10.0.2.2:3000"
    }
}