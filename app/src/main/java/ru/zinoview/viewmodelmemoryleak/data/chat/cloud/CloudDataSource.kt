package ru.zinoview.viewmodelmemoryleak.data.chat.cloud

import com.google.gson.Gson
import io.socket.client.Socket
import ru.zinoview.viewmodelmemoryleak.data.core.Observe
import ru.zinoview.viewmodelmemoryleak.core.chat.EditMessage
import ru.zinoview.viewmodelmemoryleak.data.core.cloud.AbstractCloudDataSource
import ru.zinoview.viewmodelmemoryleak.data.core.cloud.Disconnect
import ru.zinoview.viewmodelmemoryleak.data.core.cloud.Json
import ru.zinoview.viewmodelmemoryleak.data.core.cloud.SocketConnection

interface CloudDataSource<T> : Disconnect<Unit>,
    Observe<List<CloudMessage>>,
    EditMessage {

    suspend fun sendMessage(userId: Int,content: String)

    suspend fun messages(block:(List<CloudMessage>) -> Unit) : T

    class Base(
        private val socket: Socket,
        private val connection: SocketConnection,
        private val json: Json,
        private val gson: Gson,
        private val data: Data<List<CloudMessage>>,
        private val messagesStore: MessagesStore
    ) : AbstractCloudDataSource.Base(socket, connection), CloudDataSource<Unit> {

        override suspend fun sendMessage(userId: Int,content: String) {
            messagesStore.addMessage(
                CloudMessage.Progress(
                    userId,
                    content
                )
            )

            val message = json.create(
                Pair(
                    SENDER_ID_KEY,userId
                ),
                Pair(
                    MESSAGE_CONTENT_KEY,content
                )
            )

            connection.connect(socket)
            socket.emit(SEND_MESSAGE,message)
            }

        override suspend fun editMessage(messageId: String, content: String) {
            val message = json.create(
                Pair(
                    MESSAGE_ID_KEY,messageId
                ),
                Pair(
                    MESSAGE_CONTENT_KEY,content
                )
            )

            connection.connect(socket)
            socket.emit(EDIT_MESSAGE,message)
            connection.addSocketBranch(EDIT_MESSAGE)
        }


        override suspend fun observe(block: (List<CloudMessage>) -> Unit) {
            connection.connect(socket)
            messagesStore.subscribe(block)
            socket.on(SEND_MESSAGE) { data ->

                val wrapperMessages = gson.toJson(data.first())
                val messages = gson.fromJson(wrapperMessages, WrapperMessages::class.java).map()

                messagesStore.addMessages(messages)
            }
            connection.addSocketBranch(SEND_MESSAGE)
        }

        override suspend fun messages(block:(List<CloudMessage>) -> Unit) {
            connection.connect(socket)
            messagesStore.subscribe(block)

            socket.on(MESSAGES) { cloudData ->
                val wrapperMessages = gson.toJson(cloudData.first())
                val modelMessages = gson.fromJson(wrapperMessages, WrapperMessages::class.java).map()

                val messages = data.data(modelMessages)

                messagesStore.addMessages(messages)

                connection.disconnectBranch(socket, MESSAGES)
            }
            socket.emit(MESSAGES)
            connection.addSocketBranch(MESSAGES)

            observe(block)
        }

    }

    private companion object {
        private const val SEND_MESSAGE = "send_message"
        private const val MESSAGES = "messages"
        private const val EDIT_MESSAGE = "edit_message"

        private const val SENDER_ID_KEY = "senderId"
        private const val MESSAGE_CONTENT_KEY = "content"
        private const val MESSAGE_ID_KEY = "id"
    }

    class Test : CloudDataSource<List<CloudMessage>> {

        private val messages = mutableListOf<CloudMessage.Test>()
        private var isSuccess = false

        override suspend fun sendMessage(userId: Int, content: String) {
            messages.add(CloudMessage.Test(
                "-1",userId,content,"-1"
            ))
        }

        override suspend fun messages(block: (List<CloudMessage>) -> Unit) : List<CloudMessage> {
            val result = if (isSuccess) {
                messages
            } else {
                listOf(CloudMessage.Failure("Messages are empty"))
            }
            isSuccess = !isSuccess
            return result
        }

        override fun disconnect(arg: Unit) = messages.clear()

        override suspend fun observe(block: (List<CloudMessage>) -> Unit) = Unit

        override suspend fun editMessage(messageId: String, content: String) {
            val message = messages[messageId.toInt() - 1]
            messages[messageId.toInt() - 1]  = message.update(content)
        }

    }

}