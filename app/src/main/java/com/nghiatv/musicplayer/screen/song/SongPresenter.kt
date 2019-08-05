package com.nghiatv.musicplayer.screen.song

import android.os.Handler
import com.nghiatv.musicplayer.R
import com.nghiatv.musicplayer.data.model.Song
import com.nghiatv.musicplayer.data.repository.SongRepository
import com.nghiatv.musicplayer.service.*

class SongPresenter(private val view: SongContract.View) : SongContract.Presenter {

    private var mMusicService: MusicService? = null
    private var mPlayerAdapter: PlayerAdapter? = null
    private var mPlaybackListener: PlaybackListener? = null
    private lateinit var mMusicNotificationManager: MusicNotificationManager

    override fun setService(service: MusicService?) {
        mMusicService = service
    }

    override fun loadData(): MutableList<Song> {
        return SongRepository.getInstance(view.mainActivity).getAllDeviceSongs(view.mainActivity)
    }

    override fun onServiceConnected() {
        mPlayerAdapter = mMusicService?.getMediaPlayerHolder()
        mMusicNotificationManager = mMusicService?.getMusicNotificationManager()!!

        if (mPlaybackListener == null) {
            mPlaybackListener = PlaybackListener()
            mPlayerAdapter?.setPlaybackInfoListener(mPlaybackListener!!)
        }
        if (mPlayerAdapter != null && (mPlayerAdapter as MediaPlayHolder).isPlaying()) {

            restorePlayerStatus()
        }
    }

    override fun onServiceDisconnected() {
        mMusicService = null
    }

    override fun onStart() {
        if (mPlayerAdapter != null && mPlayerAdapter!!.isPlaying()) {
            restorePlayerStatus()
        }
    }

    override fun onStop() {
        if (mPlayerAdapter != null && mPlayerAdapter!!.isMediaPlayer()) {
            mPlayerAdapter!!.onPauseActivity()
        }
    }

    override fun onStopTrackingTouch(userSelectedPosition: Int) {
        mPlayerAdapter?.seekTo(userSelectedPosition)
    }

    override fun buttonPlayPauseClicked() {
        if (checkIsPlayer()) {
            mPlayerAdapter?.resumeOrPause()
        }
    }

    override fun buttonNextClicked() {
        if (checkIsPlayer()) {
            mPlayerAdapter?.skip(true)
        }
    }

    override fun buttonPreviousClicked() {
        if (checkIsPlayer()) {
            mPlayerAdapter?.instantReset()
        }
    }

    override fun onSongSelected(song: Song, songs: MutableList<Song>) {
        view.onSongSelected()

        try {
            mPlayerAdapter?.setCurrentSong(song, songs)
            mPlayerAdapter?.initMediaPlayer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun restorePlayerStatus() {
        view.updateSeekBarDisplay(mPlayerAdapter!!.isMediaPlayer())

        //if we are playing and the activity was restarted
        //update the controls panel
        if (mPlayerAdapter != null && mPlayerAdapter!!.isMediaPlayer()) {

            mPlayerAdapter!!.onResumeActivity()
            updatePlayingInfo(restore = true, startPlay = false)
        }
    }

    private fun updatePlayingInfo(restore: Boolean, startPlay: Boolean) {

        if (startPlay) {
            mPlayerAdapter!!.getMediaPlayer().start()
            Handler().postDelayed({
                mMusicService!!.startForeground(
                        MusicNotificationManager.NOTIFICATION_ID,
                        mMusicNotificationManager.createNotification()
                )
            }, 250)
        }

        val selectedSong = mPlayerAdapter?.getCurrentSong()

        view.updateSongTitleSelected(selectedSong)

        val duration = selectedSong?.duration
        if (duration != null) {
            view.updateSeekBarMax(duration)
        }

        if (restore) {
            view.updateSeekBarProgress(mPlayerAdapter!!.getPlayerPosition())

            updatePlayingStatus()

            Handler().postDelayed({
                //stop foreground if coming from pause state
                mMusicService?.run {
                    if (isRestoredFromPause()) {
                       stopForeground(false)
                        getMusicNotificationManager()!!.getNotificationManager()
                                .notify(
                                        MusicNotificationManager.NOTIFICATION_ID,
                                        getMusicNotificationManager()!!.getNotificationBuilder().build()
                                )
                        setRestoredFromPause(false)
                    }
                }
            }, 250)
        }
    }

    private fun updatePlayingStatus() {
        val drawable = if (mPlayerAdapter?.getState() != PlaybackInfoListener.State.PAUSED)
            R.drawable.ic_pause_black_24dp
        else
            R.drawable.ic_play_black_24dp

        view.updateButtonPlayPause(drawable)
    }

    private fun checkIsPlayer(): Boolean {
        val isPlayer = mPlayerAdapter?.isMediaPlayer()
        return isPlayer ?: false
    }

    private inner class PlaybackListener : PlaybackInfoListener() {

        override fun onPositionChanged(position: Int) {
            view.updateSeekBarProgress(position)
        }

        override fun onStateChanged(state: State) {
            updatePlayingStatus()
            if (mPlayerAdapter!!.getState() != State.RESUMED && mPlayerAdapter?.getState() != State.PAUSED) {
                updatePlayingInfo(restore = false, startPlay = true)
            }
        }

        override fun onPlaybackCompleted() {

        }
    }
}
