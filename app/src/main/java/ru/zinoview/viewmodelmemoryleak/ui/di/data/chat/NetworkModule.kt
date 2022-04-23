package ru.zinoview.viewmodelmemoryleak.ui.di.data.chat

import org.koin.dsl.module.module
import ru.zinoview.viewmodelmemoryleak.core.IsNotEmpty
import ru.zinoview.viewmodelmemoryleak.data.chat.cloud.*

class NetworkModule : ru.zinoview.viewmodelmemoryleak.ui.di.core.Module {

    private val networkModule = module {
        single<MessagesStore> {
            MessagesStore.Base(
                ListItem.Base(),
                ToCloudProgressMessageMapper(),
                IsNotEmpty.List(),
                ListSize.Base(),
                get()
            )
        }
        single {
            CloudDataSource.Base(
                get(),
                get(),
                get(),
                get(),
                Data.CloudMessage(),
                get()
            )
        }

        single {
            CloudDataSource.Update(
                get()
            )
        }
    }

    override fun add(modules: MutableList<org.koin.dsl.module.Module>) {
        modules.add(networkModule)
    }
}