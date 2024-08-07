package com.zktony.android.data

import com.zktony.android.R

enum class Sound(val resId: Int) {
    MUTE(R.string.app_sound_mute),
    RING(R.string.app_sound_ring),
    VOICE(R.string.app_sound_voice);

    companion object {
        fun fromName(name: String): Sound {
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

enum class SoundType(val ring: Int, val voice: Int) {
    START(R.raw.test, R.raw.test),
    STOP(R.raw.test, R.raw.test),
    SWITCH(R.raw.test, R.raw.test),
    ERROR(R.raw.error, R.raw.error),
    EXPORT(R.raw.test, R.raw.test)
}