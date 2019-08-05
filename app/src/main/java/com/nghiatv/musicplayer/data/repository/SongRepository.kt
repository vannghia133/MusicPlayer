package com.nghiatv.musicplayer.data.repository

import android.content.Context
import com.nghiatv.musicplayer.data.model.Song
import com.nghiatv.musicplayer.data.resource.SongDataSource
import com.nghiatv.musicplayer.data.resource.local.SongLocalDataSource

class SongRepository private constructor(private val songDataSource: SongDataSource): SongDataSource{
    override fun getAllDeviceSongs(context: Context): MutableList<Song> {
        return songDataSource.getAllDeviceSongs(context)
    }

    companion object {
        private var INSTANCE: SongRepository? = null

        fun getInstance(context: Context): SongRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SongRepository(SongLocalDataSource.getInstance(context))
                INSTANCE = instance
                instance
            }
        }
    }
}
