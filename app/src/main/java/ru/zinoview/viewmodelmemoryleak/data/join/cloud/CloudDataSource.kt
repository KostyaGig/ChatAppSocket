package ru.zinoview.viewmodelmemoryleak.data.join.cloud

import io.socket.client.Socket
import ru.zinoview.viewmodelmemoryleak.data.core.cloud.AbstractCloudDataSource
import ru.zinoview.viewmodelmemoryleak.data.core.cloud.Disconnect
import ru.zinoview.viewmodelmemoryleak.data.core.cloud.Json
import ru.zinoview.viewmodelmemoryleak.data.core.cloud.SocketConnection

interface CloudDataSource : Disconnect<Unit>, AbstractCloudDataSource {

    fun join(nickname: String,block: (Int) -> Unit)

    class Base(
        private val socket: Socket,
        private val connection: SocketConnection,
        private val json: Json,
    ) : AbstractCloudDataSource.Base(socket, connection), CloudDataSource {

        override fun join(nickname: String, block: (Int) -> Unit) {

                connection.connect(socket)
                connection.addSocketBranch(JOIN_USER)

                val user = json.create(
                    Pair(
                        NICKNAME_KEY,
                        nickname
                    )
                )

                socket.on(JOIN_USER) { data ->
                    val id = data.first() as Int
                    block.invoke(id)
                }
                socket.emit(JOIN_USER,user)
        }

        private companion object {
            private const val JOIN_USER = "join_user"

            private const val NICKNAME_KEY = "nickname"
        }

    }
}