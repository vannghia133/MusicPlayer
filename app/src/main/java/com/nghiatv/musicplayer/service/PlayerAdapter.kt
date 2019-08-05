package com.nghiatv.musicplayer.service

import com.nghiatv.musicplayer.data.model.Song
import android.media.MediaPlayer

interface PlayerAdapter {

    fun initMediaPlayer()

    fun release()

    fun isMediaPlayer(): Boolean

    fun isPlaying(): Boolean

    fun resumeOrPause()

    fun reset()

    fun isReset(): Boolean

    fun instantReset()

    fun skip(isNext: Boolean)

    fun seekTo(position: Int)

    fun setPlaybackInfoListener(playbackInfoListener: PlaybackInfoListener)

    fun getCurrentSong(): Song?

    fun getState(): PlaybackInfoListener.State

    fun getPlayerPosition(): Int

    fun registerNotificationActionsReceiver(isRegister: Boolean)

    fun setCurrentSong(song: Song, songs: MutableList<Song>)

    fun getMediaPlayer(): MediaPlayer

    fun onPauseActivity()

    fun onResumeActivity()
}