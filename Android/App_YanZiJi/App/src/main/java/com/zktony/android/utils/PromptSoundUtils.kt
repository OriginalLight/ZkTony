package com.zktony.android.utils

import android.app.Application
import android.media.MediaPlayer

object PromptSoundUtils {
    private var code: Int = 0

    // Set the audio
    fun setPromptSound(id: Int) {
        this.code = id
    }

    // Get the audio
    fun setPromptSound(str: String) {
        this.code = getPromptSoundId(str)
    }

    // Get the audio string
    fun getPromptSoundStr(resId: Int): String {
        return when (resId) {
            0 -> "mute"
            1 -> "ring"
            2 -> "voice"
            else -> "mute"
        }
    }

    // Get the audio code
    fun getPromptSoundId(audio: String): Int {
        return when (audio) {
            "mute" -> 0
            "ring" -> 1
            "voice" -> 2
            else -> 0
        }
    }

    // Play the audio
    fun playAudio(resId: Int) {
        // Play audio
        val mediaPlayer = MediaPlayer.create(ApplicationUtils.ctx, resId)
        mediaPlayer.setVolume(1f, 1f)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
        }
    }

    // Play the switch prompt audio
    fun playSwitchPromptSound() {
        when (code) {
            0 -> {} // mute do nothing
            1 -> {} // TODO: Play the ring audio
            2 -> {} // TODO: Play the voice audio
        }
    }
}