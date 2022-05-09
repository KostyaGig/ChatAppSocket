package ru.zinoview.viewmodelmemoryleak.ui.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.zinoview.viewmodelmemoryleak.R
import ru.zinoview.viewmodelmemoryleak.ui.chat.ChatAdapter
import ru.zinoview.viewmodelmemoryleak.ui.chat.UiMessage
import ru.zinoview.viewmodelmemoryleak.ui.core.AbstractDiffUtil
import ru.zinoview.viewmodelmemoryleak.ui.core.AbstractViewHolder
import ru.zinoview.viewmodelmemoryleak.ui.core.Adapter
import java.lang.IllegalArgumentException

class UsersAdapter(
    diffUtil: AbstractDiffUtil<UiUser>
) : Adapter<List<UiUser>>,
    androidx.recyclerview.widget.ListAdapter<UiUser, UsersAdapter.BaseViewHolder>(diffUtil) {

    object Empty : Adapter<List<UiUser>> {
        override fun update(data: List<UiUser>) = Unit
    }

    override fun update(data: List<UiUser>)
        = submitList(data)

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is UiUser.Base -> 1
            else ->
                throw IllegalArgumentException("UsersAdapter doesn't handle ${getItem(position)}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when(viewType) {
            1 -> BaseViewHolder.User(
                LayoutInflater.from(parent.context).inflate(R.layout.user,parent,false)
            )
            else -> throw IllegalArgumentException("UsersAdapter onCreateViewHolder, viewType -  $viewType")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int)
        = holder.bind(getItem(position))

    abstract class BaseViewHolder(
        view: View
    ): AbstractViewHolder<UiUser>(view) {

        class User(
            view: View
        ) : BaseViewHolder(view) {

            private val nickName = view.findViewById<TextView>(R.id.user_nickname_tv)

            override fun bind(item: UiUser) {
                item.bind(nickName)
            }
        }
    }
}