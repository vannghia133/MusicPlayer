package com.nghiatv.musicplayer.screen.song

import com.nghiatv.androidadvance.screen.BasePresenter
import com.nghiatv.musicplayer.data.model.Song
import com.nghiatv.musicplayer.service.MusicService

interface SongContract {
    interface View {
        val mainActivity: MainActivity

        fun onSongSelected()

        fun updateSongTitleSelected(selectedSong: Song?)

        fun updateButtonPlayPause(drawable: Int)

        fun updateSeekBarDisplay(isEnabled: Boolean)

        fun updateSeekBarMax(duration: Int)

        fun updateSeekBarProgress(position: Int)

    }

    interface Presenter : BasePresenter<View> {
        fun loadData() : MutableList<Song>

        fun setService(service: MusicService?)

        fun onStart()

        fun onStop()

        fun onServiceConnected()

        fun onServiceDisconnected()

        fun onStopTrackingTouch(userSelectedPosition: Int)

        fun buttonPlayPauseClicked()

        fun buttonNextClicked()

        fun buttonPreviousClicked()

        fun onSongSelected(song: Song, songs: MutableList<Song>)
    }
}
