package com.zktony.www.common.audio

import android.content.Context
import android.media.MediaPlayer

class AudioPlayer {

    fun play(context: Context, resId: Int) {
        val mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer.setVolume(1f, 1f)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
        }
    }

    companion object {
        @JvmStatic
        val instance: AudioPlayer by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AudioPlayer()
        }
    }
}