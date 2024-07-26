package com.zktony.android.data

sealed class UpgradeState {
    data class Message(val message: String) : UpgradeState()
    data class Progress(val progress: Double) : UpgradeState()
    data class Err(val t: Throwable) : UpgradeState()
    data object Success : UpgradeState()
}