package com.zktony.android.data

import com.zktony.android.R

enum class PromptSound(val resId: Int) {
    MUTE(R.string.mute),
    RING(R.string.ring),
    VOICE(R.string.voice);

    companion object {
        fun fromName(name: String): PromptSound {
            return entries.first { it.name == name }
        }

        fun indexFromName(name: String): Int {
            return entries.indexOfFirst { it.name == name }
        }

        fun getResIdList(): List<Int> {
            return entries.map { it.resId }
        }

        fun getNameByIndex(index: Int): String {
            return entries.getOrNull(index)?.name ?: MUTE.name
        }
    }
}