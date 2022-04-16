package ru.zinoview.viewmodelmemoryleak.ui.di.data.cache

import android.content.Context
import org.koin.dsl.module.module
import ru.zinoview.viewmodelmemoryleak.data.cache.Id
import ru.zinoview.viewmodelmemoryleak.data.cache.IdSharedPreferences
import ru.zinoview.viewmodelmemoryleak.data.cache.SharedPreferencesReader


class CoreCacheModule(
    private val context: Context
) : ru.zinoview.viewmodelmemoryleak.ui.di.core.Module {
    override fun add(modules: MutableList<org.koin.dsl.module.Module>) {
        modules.add(cacheModule)
    }

    private val cacheModule = module {
        single<Id> {
            Id.Base()
        }
        single<IdSharedPreferences<Int,Unit>> {
            IdSharedPreferences.Base(
                SharedPreferencesReader.Base(
                    get()
                ),
                get(),
                context
            )
        }


    }
}