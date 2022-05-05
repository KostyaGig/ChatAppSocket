package ru.zinoview.viewmodelmemoryleak.data.chat.cloud

import android.util.Log
import ru.zinoview.viewmodelmemoryleak.core.chat.Mapper
import ru.zinoview.viewmodelmemoryleak.data.cache.IdSharedPreferences
import ru.zinoview.viewmodelmemoryleak.data.chat.DataMessage

interface CloudToDataMessageMapper : Mapper<DataMessage> {

    fun map(
        id: String = "",
        senderId: Int = -1,
        content: String = "",
        senderNickname: String = "",
        isRead: Boolean
    ) : DataMessage

    class Base(
        private val idSharedPreferences: IdSharedPreferences<String, Unit>
    ) : CloudToDataMessageMapper, Mapper.Base<DataMessage>(DataMessage.Empty) {

        override fun map(
            id: String,
            senderId: Int,
            content: String,
            senderNickname: String,
            isRead: Boolean
        ): DataMessage {
            return if (idSharedPreferences.read(Unit) == senderId.toString()) {
                if (isRead) {
                    DataMessage.Sent.Read(id, senderId, content, senderNickname)
                } else {
                    DataMessage.Sent.Unread(id, senderId, content, senderNickname)
                }
            } else {
                DataMessage.Received(id, senderId, content, senderNickname)
            }
        }

        override fun mapFailure(message: String)
            = DataMessage.Failure(message)

        override fun mapProgress(senderId: Int, content: String)
            = DataMessage.Progress(senderId, content)

        override fun mapIsTyping(senderNickname: String): DataMessage {
            Log.d("zinoviewk","mapIsTyping")
            return DataMessage.Typing.Is(senderNickname)
        }

        override fun mapIsNotTyping(senderNickname: String) : DataMessage {
            Log.d("zinoviewk","mapIsNotTyping")

            return DataMessage.Typing.IsNot(senderNickname)
        }
    }
}