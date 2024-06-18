package com.zktony.android.ui.utils

/**
 * Different type of page supported by app depending on each screen.
 */
enum class PageType {
    HOME, CALIBRATION_LIST, CALIBRATION_DETAIL, HISTORY_LIST, HISTORY_DETAIL, PROGRAM_LIST, PROGRAM_DETAIL, SETTINGS, AUTH, CONFIG, MOTOR_LIST, MOTOR_DETAIL, DEBUG
}

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