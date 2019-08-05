package com.nghiatv.musicplayer.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class MusicService : Service() {

    private val mIBinder: IBinder = LocalBinder()
    private var mMediaPlayerHolder: MediaPlayHolder? = null
    private var mMusicNotificationManager: MusicNotificationManager? = null
    private var sRestoredFromPause: Boolean = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        if (mMediaPlayerHolder == null) {
            mMediaPlayerHolder = MediaPlayHolder(this)
            mMusicNotificationManager = MusicNotificationManager(this)
            mMediaPlayerHolder?.registerNotificationActionsReceiver(true)
        }
        return mIBinder
    }

    override fun onDestroy() {
        mMediaPlayerHolder?.registerNotificationActionsReceiver(false)
        mMusicNotificationManager = null
        mMediaPlayerHolder?.release()
        super.onDestroy()
    }

    fun isRestoredFromPause(): Boolean {
        return sRestoredFromPause
    }

    fun getMediaPlayerHolder(): MediaPlayHolder? {
        return mMediaPlayerHolder
    }

    fun getMusicNotificationManager(): MusicNotificationManager? {
        return mMusicNotificationManager
    }

    fun setRestoredFromPause(restore: Boolean) {
        sRestoredFromPause = restore
    }

    internal inner class LocalBinder : Binder() {
        fun getInstance() = this@MusicService
    }
}
