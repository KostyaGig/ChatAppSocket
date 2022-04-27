package ru.zinoview.viewmodelmemoryleak.ui.chat

import ru.zinoview.viewmodelmemoryleak.core.chat.Mapper


class ToUiMessageMapper : Mapper.Base<UiChatMessage>(
    UiChatMessage.Empty
) {


    override fun mapFailure(message: String): UiChatMessage
        = UiChatMessage.Failure(message)

    override fun mapReceived(
        id: String,
        senderId: Int,
        content: String,
        senderNickname: String
    ): UiChatMessage
        = UiChatMessage.Received(
            id,content,senderId.toString(),senderNickname
        )

    override fun mapProgress(senderId: Int, content: String)
        = UiChatMessage.ProgressMessage(senderId.toString(),content)

    override fun mapRead(
        id: String,
        senderId: Int,
        content: String,
        senderNickname: String
    ) = UiChatMessage.Sent.Read(id,content,senderId.toString(),senderNickname)

    override fun mapUnRead(
        id: String,
        senderId: Int,
        content: String,
        senderNickname: String
    ) =  UiChatMessage.Sent.Unread(id,content,senderId.toString(),senderNickname)

}