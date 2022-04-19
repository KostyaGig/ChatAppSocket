package ru.zinoview.viewmodelmemoryleak.ui.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.zinoview.viewmodelmemoryleak.R
import ru.zinoview.viewmodelmemoryleak.ui.chat.edit.EditMessageListener
import ru.zinoview.viewmodelmemoryleak.ui.core.AbstractDiffUtil
import ru.zinoview.viewmodelmemoryleak.ui.core.AbstractViewHolder
import java.lang.IllegalStateException

 open class ChatAdapter(
     diffUtil: AbstractDiffUtil<UiChatMessage>,
     private val listener: EditMessageListener
 ) : androidx.recyclerview.widget.ListAdapter<UiChatMessage, ChatAdapter.BaseViewHolder>(diffUtil) {

     object Empty : ChatAdapter(AbstractDiffUtil.Empty, EditMessageListener.Empty)

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is UiChatMessage.Sent.Read -> 1
            is UiChatMessage.Sent.Unread -> 2
            is UiChatMessage.ProgressMessage -> 3
            is UiChatMessage.Received -> 4
            is UiChatMessage.Progress, UiChatMessage.Empty -> 5
            is UiChatMessage.Failure -> 6
            else -> {
                Log.d("zinoviewk","else ${getItem(position)}")
                -1
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        return when(viewType) {
            1 -> BaseViewHolder.Message(
                LayoutInflater.from(parent.context).inflate(R.layout.sent_read,parent,false),
                listener
            )
            2 -> BaseViewHolder.Message(
                LayoutInflater.from(parent.context).inflate(R.layout.sent_unread,parent,false),
                listener
            )
            3 -> BaseViewHolder.Message(
                LayoutInflater.from(parent.context).inflate(R.layout.progress,parent,false),
                listener
            )
            4 -> BaseViewHolder.Message(
                LayoutInflater.from(parent.context).inflate(R.layout.received,parent,false),
                listener
            )
            5 -> BaseViewHolder.Empty(
                    LayoutInflater.from(parent.context).inflate(R.layout.empty,parent,false)
                )
            6 -> BaseViewHolder.Failure(
                LayoutInflater.from(parent.context).inflate(R.layout.error,parent,false)
            )

            else -> throw IllegalStateException("ChatAdapter.onCreateViewHolder else branch")
        }
    }

     override fun onBindViewHolder(holder: BaseViewHolder, position: Int)
        = holder.bind(getItem(position))

    abstract class BaseViewHolder(
        view: View
    ) : AbstractViewHolder<UiChatMessage>(view) {

        class Message(
            view: View,
            private val listener: EditMessageListener
        ) : BaseViewHolder(view) {

            private val contentTv = view.findViewById<TextView>(R.id.message_content_tv)
            private val stateImage = view.findViewById<ImageView>(R.id.state_send_image)
            private val editImage = view.findViewById<ImageView>(R.id.edit_image)

            override fun bind(item: UiChatMessage) {
                item.bind(contentTv,stateImage,editImage)

                editImage.setOnClickListener { item.edit(listener) }
            }
        }

        class Empty(
            view: View
        ) : BaseViewHolder(view)


        class Failure(
            view: View
        ) : BaseViewHolder(view) {
            override fun bind(item: UiChatMessage) = Unit
        }
    }



}
