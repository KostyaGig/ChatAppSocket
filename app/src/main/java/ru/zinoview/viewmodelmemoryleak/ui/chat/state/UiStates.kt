package ru.zinoview.viewmodelmemoryleak.ui.chat.state

import ru.zinoview.viewmodelmemoryleak.data.chat.cloud.CloudMessage
import ru.zinoview.viewmodelmemoryleak.ui.chat.ToUiMessageMapper

interface UiStates {

    fun map(communication: UiStateCommunication) = Unit

    fun map(mapper: ToUiMessageMapper,messages: List<CloudMessage>) : UiStates = Base()

    class Base(
        private val messageField: UiState.EditText = UiState.EditText(),
        private val editMessage: UiState.MessageSession = UiState.MessageSession(),
//        private val messages: UiState.Messages = UiState.Messages()
    ) : UiStates {

        override fun map(communication: UiStateCommunication) {
            val states = mutableListOf<UiState>()

            if (messageField.isNotEmpty(Unit)) {
                states.add(messageField)
            }

            if (editMessage.isNotEmpty(Unit)) {
                states.add(editMessage)
            }

//            if (messages.isNotEmpty(Unit)) {
//                states.add(messages)
//            }

            communication.postValue(states)
        }

    }

    interface Test : UiStates {

        fun map() : UiStates = Empty

        data class Base(
            private val messageField: UiState.EditText = UiState.EditText(),
            private val editMessage: UiState.MessageSession = UiState.MessageSession()
        ) : Test {

            override fun map() : UiStates {
                return if (messageField.isNotEmpty(Unit) && editMessage.isNotEmpty(Unit)) {
                    this
                } else {
                    return if (messageField.isNotEmpty(Unit) && editMessage.isNotEmpty(Unit).not()) {
                        Base(messageField, editMessage)
                    } else if (messageField.isNotEmpty(Unit).not() && editMessage.isNotEmpty(Unit)){
                        Base(messageField, editMessage)
                    } else {
                        Empty
                    }
                }
            }
        }

        object Empty : Test
    }



}