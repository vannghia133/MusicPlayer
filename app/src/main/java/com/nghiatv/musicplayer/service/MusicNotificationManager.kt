package com.nghiatv.musicplayer.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.media.session.MediaSessionManager
import android.support.v4.app.NotificationCompat
import android.support.v4.media.session.MediaControllerCompat
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.nghiatv.musicplayer.R
import com.nghiatv.musicplayer.data.model.Song
import com.nghiatv.musicplayer.screen.song.MainActivity
import com.nghiatv.musicplayer.utils.ImageUtils
import android.app.NotificationChannel
import android.support.annotation.RequiresApi
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.MediaMetadataCompat

class MusicNotificationManager(musicService: MusicService) {

    companion object {
        const val NOTIFICATION_ID = 101
        const val PLAY_PAUSE_ACTION = "action.PLAY_PAUSE"
        const val NEXT_ACTION = "action.NEXT"
        const val PREV_ACTION = "action.PREV"
    }

    private val CHANNEL_ID = "action.CHANNEL_ID"
    private val REQUEST_CODE = 100
    private val mNotificationManager: NotificationManager
    private val mMusicService: MusicService = musicService
    private lateinit var mNotificationBuilder: NotificationCompat.Builder
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var transportControls: MediaControllerCompat.TransportControls
    private val context: Context

    init {
        mNotificationManager = mMusicService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        context = musicService.baseContext
    }

    fun getNotificationManager(): NotificationManager {
        return mNotificationManager
    }

    fun getNotificationBuilder(): NotificationCompat.Builder {
        return mNotificationBuilder
    }

    fun createNotification(): Notification {
        val song: Song? = mMusicService.getMediaPlayerHolder()?.getCurrentSong()
        mNotificationBuilder = NotificationCompat.Builder(mMusicService, CHANNEL_ID)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val openPlayerIntent = Intent(mMusicService, MainActivity::class.java)
        openPlayerIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val contentIntent: PendingIntent = PendingIntent.getActivity(
            mMusicService,
            REQUEST_CODE,
            openPlayerIntent,
            0
        )

        val artist = song?.artistName
        val songTitle: String? = song?.title

        if (song != null) {
            initMediaSession(song)
        }

        mNotificationBuilder.run {
            setShowWhen(false)
            setSmallIcon(R.drawable.ic_play_black_24dp)
            setLargeIcon(ImageUtils.songArt(song?.path!!, mMusicService.baseContext))
            color = context.resources.getColor(R.color.colorAccent)
            setContentTitle(songTitle)
            setContentText(artist)
            setContentIntent(contentIntent)
            addAction(notificationAction(PREV_ACTION))
            addAction(notificationAction(PLAY_PAUSE_ACTION))
            addAction(notificationAction(NEXT_ACTION))
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        mNotificationBuilder.apply {
            setStyle(
                android.support.v4.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
        }

        return mNotificationBuilder.build()
    }

    private fun playerAction(action: String): PendingIntent {
        val pauseIntent = Intent()
        pauseIntent.action = action

        return PendingIntent.getBroadcast(
            mMusicService,
            REQUEST_CODE,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun notificationAction(action: String): NotificationCompat.Action {
        var icon: Int = -1
        when (action) {
            PREV_ACTION -> icon = R.drawable.ic_skip_previous_black_24dp
            PLAY_PAUSE_ACTION -> icon =
                if (mMusicService.getMediaPlayerHolder()!!.getState() != PlaybackInfoListener.State.PAUSED)
                    R.drawable.ic_pause_black_24dp else R.drawable.ic_play_black_24dp
            NEXT_ACTION -> icon = R.drawable.ic_skip_next_black_24dp
        }
        return NotificationCompat.Action.Builder(icon, action, playerAction(action)).build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                mMusicService.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            )

            notificationChannel.run {
                description = mMusicService.getString(com.nghiatv.musicplayer.R.string.app_name)
                notificationChannel.enableLights(false)
                notificationChannel.enableVibration(false)
                notificationChannel.setShowBadge(false)
            }

            mNotificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun initMediaSession(song: Song) {
        mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        mediaSession = MediaSessionCompat(context, "AudioPlayer")
        transportControls = mediaSession.controller.transportControls
        mediaSession.isActive = true
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        updateMetaData(song)
    }

    private fun updateMetaData(song: Song) {
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder().run {
                putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, ImageUtils.songArt(song.path!!, context))
                putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artistName)
                putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.albumName)
                putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                build()
            }

        )
    }

}
