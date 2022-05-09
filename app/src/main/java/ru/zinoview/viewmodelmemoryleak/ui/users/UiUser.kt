package ru.zinoview.viewmodelmemoryleak.ui.users

import android.graphics.Bitmap
import android.widget.TextView
import ru.zinoview.viewmodelmemoryleak.ui.core.*

sealed class UiUser : DiffSame<UiUser>, SameOne<String>, Same<String,Unit>, Bind<TextView> {

    override fun isContentTheSame(item: UiUser) = false
    override fun isItemTheSame(item: UiUser) = false
    override fun same(data: String) = false
    override fun same(arg1: String, arg2: Unit) = false

    override fun bind(view: TextView) = Unit
    class Base(
        private val id: String,
        private val nickName: String,
        private val image: Bitmap,
    ): UiUser() {

        override fun isContentTheSame(item: UiUser)
            = item.same(id)

        override fun isItemTheSame(item: UiUser)
            = item.same(nickName,Unit)

        override fun same(id: String)
            = this.id == id

        override fun same(nickName: String, arg2: Unit)
            = this.nickName == nickName

        override fun bind(view: TextView) {
            view.text = nickName
        }

    }
}