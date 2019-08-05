package com.nghiatv.musicplayer.screen

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.nghiatv.musicplayer.R
import com.nghiatv.musicplayer.data.model.Song
import com.nghiatv.musicplayer.data.repository.SongRepository
import com.nghiatv.musicplayer.service.MusicNotificationManager
import com.nghiatv.musicplayer.service.MusicService
import com.nghiatv.musicplayer.service.PlaybackInfoListener
import com.nghiatv.musicplayer.service.PlayerAdapter
import com.nghiatv.musicplayer.utils.EqualizerUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_control.*


class MainActivity : AppCompatActivity(),
    SongContract.View,
    View.OnClickListener,
    SongAdapter.SongClicked {

    private var mMusicService: MusicService? = null
    private var mIsBound: Boolean = false
    private var mPlayerAdapter: PlayerAdapter? = null
    private var mUserIsSeeking: Boolean = false
    private var mPlaybackListener: PlaybackListener? = null
    private lateinit var mSelectedArtistSongs: MutableList<Song>
    private lateinit var mMusicNotificationManager: MusicNotificationManager
    private lateinit var songAdapter: SongAdapter
    private val mConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mMusicService = (service as MusicService.LocalBinder).getInstance()
            mPlayerAdapter = mMusicService!!.getMediaPlayerHolder()!!
            mMusicNotificationManager = mMusicService!!.getMusicNotificationManager()!!

            if (mPlaybackListener == null) {
                mPlaybackListener = PlaybackListener()
                mPlayerAdapter!!.setPlaybackInfoListener(mPlaybackListener!!)
            }
            if (mPlayerAdapter != null && mPlayerAdapter!!.isPlaying()) {

                restorePlayerStatus()
            }
            checkReadStoragePermissions()
        }


        override fun onServiceDisconnected(name: ComponentName?) {
            mMusicService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        doBindService()
        initViews()
        initializeSeekBar()
    }

    override fun onResume() {
        super.onResume()
        doBindService()
        if (mPlayerAdapter != null && mPlayerAdapter!!.isPlaying()) {

            restorePlayerStatus()
        }
    }

    override fun onPause() {
        super.onPause()
        doUnbindService()
        if (mPlayerAdapter != null && mPlayerAdapter!!.isMediaPlayer()) {
            mPlayerAdapter!!.onPauseActivity()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_DENIED
        ) {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonPlayPause -> resumeOrPause()

            R.id.buttonNext -> skipNext()

            R.id.buttonPrevious -> skipPrev()
        }
    }

    override fun onSongClicked(song: Song) {
        onSongSelected(song, mSelectedArtistSongs)
    }

    private fun doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection)
            mIsBound = false
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

        val selectedSong = mPlayerAdapter!!.getCurrentSong()

        textSongTitle.text = selectedSong.title
        val duration = selectedSong.duration
        seekBar.max = duration

        if (restore) {
            seekBar.progress = mPlayerAdapter!!.getPlayerPosition()
            updatePlayingStatus()


            Handler().postDelayed({
                //stop foreground if coming from pause state
                if (mMusicService!!.isRestoredFromPause()) {
                    mMusicService!!.stopForeground(false)
                    mMusicService!!.getMusicNotificationManager()!!.getNotificationManager()
                        .notify(
                            MusicNotificationManager.NOTIFICATION_ID,
                            mMusicService!!.getMusicNotificationManager()!!.getNotificationBuilder().build()
                        )
                    mMusicService!!.setRestoredFromPause(false)
                }
            }, 250)
        }
    }

    private fun checkReadStoragePermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), 1)
        }
    }

    private fun updatePlayingStatus() {
        val drawable = if (mPlayerAdapter!!.getState() != PlaybackInfoListener.State.PAUSED)
            R.drawable.ic_pause_black_24dp
        else
            R.drawable.ic_play_black_24dp
        buttonPlayPause.post { buttonPlayPause.setImageResource(drawable) }
    }

    private fun restorePlayerStatus() {
        seekBar.isEnabled = mPlayerAdapter!!.isMediaPlayer()

        //if we are playing and the activity was restarted
        //update the controls panel
        if (mPlayerAdapter != null && mPlayerAdapter!!.isMediaPlayer()) {

            mPlayerAdapter!!.onResumeActivity()
            updatePlayingInfo(true, false)
        }
    }

    private fun doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(
            Intent(
                this,
                MusicService::class.java
            ), mConnection, Context.BIND_AUTO_CREATE
        )
        mIsBound = true

        val startNotStickyIntent = Intent(this, MusicService::class.java)
        startService(startNotStickyIntent)
    }

    private fun initializeSeekBar() {
        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                var userSelectedPosition = 0

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    mUserIsSeeking = true
                }

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                    if (fromUser) {
                        userSelectedPosition = progress

                    }

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                    if (mUserIsSeeking) {

                    }
                    mUserIsSeeking = false
                    mPlayerAdapter!!.seekTo(userSelectedPosition)
                }
            })
    }

    private fun checkIsPlayer(): Boolean {
        val isPlayer = mPlayerAdapter!!.isMediaPlayer()
        if (!isPlayer) {
            EqualizerUtils.notifyNoSessionId(this)
        }
        return isPlayer
    }

    private fun onSongSelected(song: Song, songs: MutableList<Song>) {
        if (!seekBar.isEnabled) {
            seekBar.isEnabled = true
        }
        try {
            mPlayerAdapter!!.setCurrentSong(song, songs)
            mPlayerAdapter!!.initMediaPlayer()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun skipPrev() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.instantReset()
        }
    }

    private fun resumeOrPause() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.resumeOrPause()
        }
    }

    private fun skipNext() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.skip(true)
        }
    }

    private fun initViews() {
        buttonPrevious.setOnClickListener(this)
        buttonPlayPause.setOnClickListener(this)
        buttonNext.setOnClickListener(this)

        songAdapter = SongAdapter(this)
        recyclerView.run {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = songAdapter
        }

        mSelectedArtistSongs = SongRepository.getInstance(this).getAllDeviceSongs(this)
        songAdapter.addSongs(mSelectedArtistSongs)
    }

    private inner class PlaybackListener : PlaybackInfoListener() {

        override fun onPositionChanged(position: Int) {
            if (!mUserIsSeeking) {
                seekBar.progress = position
            }
        }

        override fun onStateChanged(state: Int) {
            updatePlayingStatus()
            if (mPlayerAdapter!!.getState() != State.RESUMED && mPlayerAdapter!!.getState() != State.PAUSED) {
                updatePlayingInfo(restore = false, startPlay = true)
            }
        }

        override fun onPlaybackCompleted() {

        }
    }
}
