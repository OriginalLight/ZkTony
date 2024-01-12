package com.zktony.core.ext

import android.content.Context

fun Context.playAudio(resId: Int) {
    val mediaPlayer = android.media.MediaPlayer.create(this, resId)
    mediaPlayer.setVolume(1f, 1f)
    mediaPlayer.start()
    mediaPlayer.setOnCompletionListener {
        mediaPlayer.release()
    }
}