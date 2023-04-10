package com.zktony.common.audio

import android.media.MediaPlayer
import com.zktony.common.ext.Ext

class AudioPlayer {

    fun play(resId: Int) {
        val mediaPlayer = MediaPlayer.create(Ext.ctx, resId)
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