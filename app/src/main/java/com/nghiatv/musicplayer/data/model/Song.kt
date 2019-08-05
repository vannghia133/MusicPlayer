package com.nghiatv.musicplayer.data.model

import java.util.*
import java.util.concurrent.TimeUnit

class Song(
    val title: String,
    val trackNumber: Int,
    val year: Int,
    val duration: Int,
    val path: String?,
    val albumName: String,
    val artistId: Int,
    val artistName: String
) {
    companion object {
        fun EMPTY_SONG() = Song(
            "",
            -1,
            -1,
            -1,
            null,
            "",
            -1,
            ""
        )

        fun formatDuration(duration: Long) = String.format(
            Locale.getDefault(), "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(duration)
            )
        )

        fun formatTrack(trackNumber: Int): Int {
            var formatted: Int = trackNumber
            if (trackNumber >= 1000) {
                formatted = trackNumber % 1000
            }
            return formatted
        }
    }
}
