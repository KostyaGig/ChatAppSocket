package ru.zinoview.viewmodelmemoryleak.data.chat.cloud

import android.util.Log
import ru.zinoview.viewmodelmemoryleak.core.IsNotEmpty
import ru.zinoview.viewmodelmemoryleak.core.chat.EditMessage
import ru.zinoview.viewmodelmemoryleak.data.cache.IdSharedPreferences
import ru.zinoview.viewmodelmemoryleak.data.core.cloud.Subscribe

interface MessagesStore : Subscribe<List<CloudMessage>>, EditMessage {

    fun addMessage(cloudMessage: CloudMessage)

    fun addMessages(messages: List<CloudMessage>)

    fun unreadMessageIds(range: Pair<Int, Int>, block: (List<String>) -> Unit)

    class Base(
        private val listItem: ListItem<CloudMessage>,
        private val mapper: ToCloudProgressMessageMapper,
        private val isNotEmpty: IsNotEmpty<List<CloudMessage>>,
        private val listSize: ListSize,
        private val idSharedPreferences: IdSharedPreferences<Int,Unit>
    ) : MessagesStore {

        private val messages = ArrayList<CloudMessage>()
        private var block : (List<CloudMessage>) -> Unit = {}

        override fun addMessage(cloudMessage: CloudMessage) {
            messages.add(cloudMessage)
            block.invoke(
                ArrayList(messages)
            )
        }

        override fun addMessages(messages: List<CloudMessage>) {
            this.messages.clear()
            this.messages.addAll(messages)
            block.invoke(
                ArrayList(messages)
            )
        }

        override suspend fun editMessage(messageId: String, content: String) {
            val messageById = listItem.item(messages,messageId)
            val editedMessageById = messageById.map(content,mapper)

            val indexMessageById = listItem.index(messages,messageId)
            this.messages[indexMessageById] = editedMessageById

            block.invoke(ArrayList(messages))
        }

        override fun unreadMessageIds(range: Pair<Int, Int>, block: (List<String>) -> Unit) {
            val userId = idSharedPreferences.read(Unit)
            val unreadMessageIds = mutableListOf<String>()

            if (isNotEmpty.isNotEmpty(messages)) {
                if (listSize.isLessThen(2,messages)) {
                    Log.d("zinoviewk","less than 2, ${messages.first()}")
                    val message = messages.first()
                    message.addUnreadMessageId(userId,unreadMessageIds)
                } else {
                    Log.d("zinoviewk","list is not empty")
                    for (index in range.first..range.second) {
                        val message = messages[index]
                        message.addUnreadMessageId(userId,unreadMessageIds)
                    }
                }
            } else {
                Log.d("zinoviewk","messages list is empty")
            }
            block.invoke(unreadMessageIds)
        }


        override fun subscribe(block: (List<CloudMessage>) -> Unit) {
            this.block = block
        }
    }
}