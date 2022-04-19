package ru.zinoview.viewmodelmemoryleak.core.chat

interface Mapper<T> {

    fun map(
        id: String = "",
        senderId: Int = -1,
        content: String = "",
        senderNickname: String = ""
    ) : T

    fun mapFailure(message: String) : T

    fun mapProgress(
        senderId: Int = -1,
        content: String = "",
    ) : T

    fun mapReceived(
        id: String,
        senderId: Int,
        content: String,
        senderNickname: String
    ): T

    // todo remove
    fun mapSent(
        id: String,
        senderId: Int,
        content: String,
        senderNickname: String
    ) : T

    fun mapRead(
        id: String,
        senderId: Int,
        content: String,
        senderNickname: String
    ) : T

    fun mapUnRead(
        id: String,
        senderId: Int,
        content: String,
        senderNickname: String
    ) : T
}