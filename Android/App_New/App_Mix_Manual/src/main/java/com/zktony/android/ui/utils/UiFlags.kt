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
    data class Objects(val objects: Int) : UiFlags()

    data class RunState(val run: Boolean) : UiFlags()
    data class Glue(val bar: Float) : UiFlags()


    companion object {
        fun none() = None
        fun loading() = Loading
        fun error(message: String) = Error(message)
        fun message(message: String) = Message(message)
        fun objects(objects: Int) = Objects(objects)
        fun runstate(run: Boolean) = RunState(run)
        fun glue(bar: Float) = Glue(bar)
    }
}