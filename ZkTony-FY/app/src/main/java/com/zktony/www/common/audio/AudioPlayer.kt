package com.zktony.www.common.audio

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AudioPlayer {
    private var isPlaying = false

    suspend fun play(context: Context, resId: Int) {
        coroutineScope {
            launch {
                if (!isPlaying) {
                    val mediaPlayer = MediaPlayer.create(context, resId)
                    isPlaying = true
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener {
                        mediaPlayer.release()
                        isPlaying = false
                    }
                } else {
                    delay(1000)
                    play(context, resId)
                }
            }
        }

    }

    companion object {
        @JvmStatic
        val instance: AudioPlayer by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AudioPlayer()
        }
    }
}