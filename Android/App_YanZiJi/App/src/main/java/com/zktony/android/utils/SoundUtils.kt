package com.zktony.android.utils

import android.media.AudioAttributes
import android.media.SoundPool
import com.zktony.android.data.Sound
import com.zktony.android.data.SoundType

object SoundUtils {
    private var soundPool: SoundPool? = null
    private var sound: Sound = Sound.VOICE

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

    // Set the audio type
    fun with(name: String) {
        sound = Sound.fromName(name)
    }

    // Play the audio with the specified resource ID
    fun play(resId: Int) {
        soundPool?.load(ApplicationUtils.ctx, resId, 1)
        soundPool?.setOnLoadCompleteListener { soundPool, sampleId, _ ->
            soundPool.play(sampleId, 1f, 1f, 1, 0, 1f)
        }
    }

    // Play the audio with the specified SoundType
    fun play(type: SoundType) {
        when (sound) {
            Sound.RING -> {
                play(type.ring)
            }

            Sound.VOICE -> {
                play(type.voice)
            }

            Sound.MUTE -> {
                // Do nothing
            }
        }
    }
}