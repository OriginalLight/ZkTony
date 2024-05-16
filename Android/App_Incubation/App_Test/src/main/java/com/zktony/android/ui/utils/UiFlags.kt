package com.zktony.android.ui.utils

/**
 * @author 刘贺贺
 * @date 2023/9/7 13:17
 */
sealed class UiFlags {
    data object None : UiFlags()
    data object Loading : UiFlags()
    data class Error(val message: String) : UiFlags()
    data class Message(val message: String) : UiFlags()

    companion object {
        fun none() = None
        fun loading() = Loading
        fun error(message: String) = Error(message)
        fun message(message: String) = Message(message)
    }
}