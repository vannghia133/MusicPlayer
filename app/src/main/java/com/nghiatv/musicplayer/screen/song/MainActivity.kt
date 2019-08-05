package com.nghiatv.musicplayer.screen.song

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.SeekBar
import com.nghiatv.musicplayer.R
import com.nghiatv.musicplayer.data.model.Song
import com.nghiatv.musicplayer.screen.BaseActivity
import com.nghiatv.musicplayer.service.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_control.*

class MainActivity : BaseActivity(),
        SongContract.View,
        View.OnClickListener,
        SongAdapter.SongClicked {

    private lateinit var presenter: SongContract.Presenter

    private var mMusicService: MusicService? = null
    private var mIsBound: Boolean = false
    private var mUserIsSeeking: Boolean = false
    private lateinit var mSongs: MutableList<Song>
    private lateinit var songAdapter: SongAdapter
    private val mConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mMusicService = (service as MusicService.LocalBinder).getInstance()
            presenter.setService(mMusicService)
            presenter.onServiceConnected()
        }


        override fun onServiceDisconnected(name: ComponentName?) {
            presenter.onServiceDisconnected()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = SongPresenter(this)

        checkReadStoragePermissions()
        bindService()
        initSeekBar()
    }

    override fun onStart() {
        super.onStart()
        bindService()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        unbindService()
        presenter.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1
                && grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            initViews()
        } else {
            finish()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonPlayPause -> presenter.buttonPlayPauseClicked()

            R.id.buttonNext -> presenter.buttonNextClicked()

            R.id.buttonPrevious -> presenter.buttonPreviousClicked()
        }
    }

    override fun onSongClicked(song: Song) {
        presenter.onSongSelected(song, mSongs)
    }

    override val mainActivity: MainActivity
        get() = this

    override fun onSongSelected() {
        if (!seekBar.isEnabled) {
            seekBar.isEnabled = true
        }
    }

    override fun updateSongTitleSelected(selectedSong: Song?) {
        textSongTitleSelected.text = selectedSong?.title
    }

    override fun updateButtonPlayPause(drawable: Int) {
        buttonPlayPause.post { buttonPlayPause.setImageResource(drawable) }
    }

    override fun updateSeekBarDisplay(isEnabled: Boolean) {
        seekBar.isEnabled = isEnabled
    }

    override fun updateSeekBarMax(duration: Int) {
        seekBar.max = duration
    }

    override fun updateSeekBarProgress(position: Int) {
        if (!mUserIsSeeking) {
            seekBar.progress = position
        }
    }

    private fun initViews() {
        textSongTitleSelected.isSelected = true

        buttonPrevious.setOnClickListener(this)
        buttonPlayPause.setOnClickListener(this)
        buttonNext.setOnClickListener(this)

        songAdapter = SongAdapter(this)
        recyclerView.run {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = songAdapter
        }
        mSongs = presenter.loadData()
        songAdapter.addSongs(mSongs)
    }

    private fun initSeekBar() {
        seekBar.isEnabled = false
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
                        presenter.onStopTrackingTouch(userSelectedPosition)
                    }
                })
    }

    private fun checkReadStoragePermissions() {
        if (ContextCompat.checkSelfPermission(
                        this,
                        READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), 1)
        } else {
            initViews()
        }
    }

    private fun bindService() {
        bindService(
                Intent(this, MusicService::class.java),
                mConnection,
                Context.BIND_AUTO_CREATE
        )
        mIsBound = true

        val intent = Intent(this, MusicService::class.java)
        startService(intent)
    }

    private fun unbindService() {
        if (mIsBound) {
            unbindService(mConnection)
            mIsBound = false
        }
    }

}
