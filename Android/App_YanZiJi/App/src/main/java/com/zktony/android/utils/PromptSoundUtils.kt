package com.zktony.android.utils

import android.media.AudioAttributes
import android.media.SoundPool
import com.zktony.android.data.PromptSound

object PromptSoundUtils {
    private var soundPool: SoundPool? = null
    private var promptSound: PromptSound = PromptSound.MUTE

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
    fun with(name: String) {
        promptSound = PromptSound.fromName(name)
    }

    fun playSound(resId: Int) {
        soundPool?.load(ApplicationUtils.ctx, resId, 1)
        soundPool?.setOnLoadCompleteListener { soundPool, sampleId, _ ->
            soundPool.play(sampleId, 1f, 1f, 1, 0, 1f)
        }
    }

    // Play the switch prompt audio
    fun playSwitchPromptSound() {
        when (promptSound) {
            PromptSound.RING -> {
            }

            PromptSound.VOICE -> {
            }

            PromptSound.MUTE -> {
                // Do nothing
            }
        }
    }
}