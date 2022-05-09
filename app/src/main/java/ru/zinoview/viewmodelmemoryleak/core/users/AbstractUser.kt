package ru.zinoview.viewmodelmemoryleak.core.users

import android.graphics.Bitmap
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

interface AbstractUser {

    fun <T> map(mapper: UserMapper<T>) : T

    class Base(
        private val id: String,
        private val nickName: String,
        private val image: Bitmap
    ) : AbstractUser {

        override fun <T> map(mapper: UserMapper<T>)
            = mapper.map(id,nickName, image)
    }

    data class Test(
        private val id: String,
        private val nickName: String,
    ) : AbstractUser {
        override fun <T> map(mapper: UserMapper<T>) = throw IllegalStateException("AbstractUser.Test.map()")
    }
}