package com.nghiatv.musicplayer.data.resource.local

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.nghiatv.musicplayer.data.model.Song
import com.nghiatv.musicplayer.data.resource.SongDataSource

class SongLocalDataSource private constructor(context: Context) : SongDataSource {

    private val TITLE = 0
    private val TRACK_NUMBER = 1
    private val YEAR = 2
    private val DURATION = 3
    private val PATH = 4
    private val ALBUM_NAM = 5
    private val ARTIST_ID = 6
    private val ARTIST_NAME = 7

    private val projection = arrayOf(
        MediaStore.Audio.AudioColumns.TITLE,
        MediaStore.Audio.AudioColumns.TRACK,
        MediaStore.Audio.AudioColumns.YEAR,
        MediaStore.Audio.AudioColumns.DURATION,
        MediaStore.Audio.AudioColumns.DATA,
        MediaStore.Audio.AudioColumns.ALBUM,
        MediaStore.Audio.AudioColumns.ARTIST_ID,
        MediaStore.Audio.AudioColumns.ARTIST
    )

    private val mAllDeviceSongs: MutableList<Song> = ArrayList()

    override fun getAllDeviceSongs(context: Context): MutableList<Song> {
        val cursor: Cursor? = makeSongCursor(context)
        return getSongs(cursor)
    }

    private fun getSongs(cursor: Cursor?): MutableList<Song> {
        val songs: MutableList<Song> = ArrayList()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val song: Song = getSongFromCursorImpl(cursor)
                if (song.duration >= 30000) {
                    songs.add(song)
                    mAllDeviceSongs.add(song)
                }
            } while (cursor.moveToNext())
        }

        cursor?.close()

        return songs
    }

    private fun getSongFromCursorImpl(cursor: Cursor): Song {
        val title: String = cursor.getString(TITLE)
        val trackNumber: Int = cursor.getInt(TRACK_NUMBER)
        val year: Int = cursor.getInt(YEAR)
        val duration: Int = cursor.getInt(DURATION)
        val path: String = cursor.getString(PATH)
        val albumName: String = cursor.getString(ALBUM_NAM)
        val artistId: Int = cursor.getInt(ARTIST_ID)
        val artistName: String = cursor.getString(ARTIST_NAME)

        return Song(
            title,
            trackNumber,
            year,
            duration,
            path,
            albumName,
            artistId,
            artistName
        )
    }

    private fun makeSongCursor(context: Context): Cursor? {
        return try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )
        } catch (e: SecurityException) {
            null
        }
    }

    companion object {
        private var INSTANCE: SongDataSource? = null

        fun getInstance(context: Context): SongDataSource {
            return INSTANCE ?: synchronized(this) {
                val instance = SongLocalDataSource(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
