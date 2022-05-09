package ru.zinoview.viewmodelmemoryleak.data.users.cloud

import android.util.Log
import ru.zinoview.viewmodelmemoryleak.core.Data
import ru.zinoview.viewmodelmemoryleak.core.cloud.SocketData
import ru.zinoview.viewmodelmemoryleak.core.cloud.SocketWrapper
import ru.zinoview.viewmodelmemoryleak.data.chat.cloud.WrapperMessages
import ru.zinoview.viewmodelmemoryleak.data.core.cloud.Json
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface CloudDataSource : Data<List<CloudUser>> {

    class Base(
        private val socketWrapper: SocketWrapper,
        private val json: Json,
    ) : CloudDataSource {

        override suspend fun data(): List<CloudUser> = suspendCoroutine {  continuation ->
            socketWrapper.subscribe(USERS) { cloudData ->
                val stringJson = json.json(cloudData.first())
                val users = json.objectFromJson(stringJson,WrapperMessages::class.java).map()
                Log.d("zinoviewk","Users - $users")
                socketWrapper.disconnect(USERS)
            }
            socketWrapper.emit(USERS,SocketData.Empty)
        }

        private companion object {
            private const val USERS = "users"
        }
    }
}