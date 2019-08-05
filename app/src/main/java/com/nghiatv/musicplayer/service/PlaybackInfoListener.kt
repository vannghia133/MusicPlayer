package com.nghiatv.musicplayer.service

abstract class PlaybackInfoListener {
    abstract fun onPositionChanged(position: Int)

    abstract fun onStateChanged(state: State)

    abstract fun onPlaybackCompleted()

    enum class State {
        PLAYING,
        PAUSED,
        COMPLETED,
        RESUMED,
    }
}
