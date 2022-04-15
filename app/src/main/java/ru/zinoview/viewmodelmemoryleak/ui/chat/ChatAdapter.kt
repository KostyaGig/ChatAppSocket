package ru.zinoview.viewmodelmemoryleak.ui.chat

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
            is UiChatMessage.Sent -> 1
            is UiChatMessage.Received -> 2
            is UiChatMessage.Progress -> 3
            is UiChatMessage.Failure -> 4
            is UiChatMessage.ProgressMessage -> 5
            else -> -1
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        return when(viewType) {
            1, 5 -> BaseViewHolder.Message(
                LayoutInflater.from(parent.context).inflate(R.layout.sent,parent,false),
                listener
            )
            2 -> BaseViewHolder.Message(
                LayoutInflater.from(parent.context).inflate(R.layout.received,parent,false),
                listener
            ) 4 -> BaseViewHolder.Failure(
                LayoutInflater.from(parent.context).inflate(R.layout.error,parent,false)
            )
            3 -> {
                BaseViewHolder.Empty(
                    LayoutInflater.from(parent.context).inflate(R.layout.empty,parent,false)
                )
            }
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