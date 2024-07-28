package com.zktony.android.utils

import android.media.AudioAttributes
import android.media.SoundPool
import com.zktony.android.data.Sound

object SoundUtils {
    private var soundPool: SoundPool? = null
    private var sound: Sound = Sound.MUTE

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
        sound = Sound.fromName(name)
    }

    fun playSound(resId: Int) {
        soundPool?.load(ApplicationUtils.ctx, resId, 1)
        soundPool?.setOnLoadCompleteListener { soundPool, sampleId, _ ->
            soundPool.play(sampleId, 1f, 1f, 1, 0, 1f)
        }
    }

    // Play the switch prompt audio
    fun playSwitchPromptSound() {
        when (sound) {
            Sound.RING -> {
            }

            Sound.VOICE -> {
            }

            Sound.MUTE -> {
                // Do nothing
            }
        }
    }
}