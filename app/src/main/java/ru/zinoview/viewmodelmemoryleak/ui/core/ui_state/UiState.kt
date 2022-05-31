package ru.zinoview.viewmodelmemoryleak.ui.core.ui_state

import ru.zinoview.viewmodelmemoryleak.ui.chat.UiMessage
import ru.zinoview.viewmodelmemoryleak.ui.chat.view.ViewWrapper
import ru.zinoview.viewmodelmemoryleak.ui.core.Adapter

interface UiState {

    fun recover(
        editText: ViewWrapper,
        viewWrapper: ViewWrapper,
        messageSession: ru.zinoview.viewmodelmemoryleak.ui.chat.edit.MessageSession,
        adapter: Adapter<List<UiMessage>>
    ) = Unit

    fun recover(image: ViewWrapper,text: ViewWrapper) = Unit

}