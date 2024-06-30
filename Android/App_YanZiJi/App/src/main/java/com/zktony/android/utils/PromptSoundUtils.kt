package com.zktony.android.utils

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

object PromptSoundUtils {
    private var soundPool: SoundPool? = null
    private var code: Int = 0

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

    }

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

    fun playSound(resId: Int) {
        soundPool?.load(ApplicationUtils.ctx, resId, 1)
        soundPool?.setOnLoadCompleteListener { soundPool, sampleId, _ ->
            soundPool.play(sampleId, 1f, 1f, 1, 0, 1f)
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