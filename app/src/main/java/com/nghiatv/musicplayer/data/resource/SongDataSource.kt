package com.nghiatv.musicplayer.data.resource

import android.content.Context
import com.nghiatv.musicplayer.data.model.Song

interface SongDataSource {
    fun getAllDeviceSongs(context: Context): MutableList<Song>
}