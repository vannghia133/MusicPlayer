package com.nghiatv.musicplayer.screen.song

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nghiatv.androidadvance.screen.BaseRecyclerViewAdapter
import com.nghiatv.musicplayer.R
import com.nghiatv.musicplayer.data.model.Song
import kotlinx.android.synthetic.main.item_song.view.*

class SongAdapter(clicked: SongClicked) : BaseRecyclerViewAdapter<SongAdapter.ViewHolder>() {

    private var songsList: MutableList<Song> = ArrayList()
    private val songClicked: SongClicked = clicked

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val song: Song = songsList[position]
        viewHolder.bind(song)
        viewHolder.itemView.setOnClickListener {
            songClicked.onSongClicked(song)
        }
    }

    fun addSongs(songs: MutableList<Song>) {
        songsList.clear()
        songsList = songs
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(song: Song) {
            itemView.textSongTitle.text = song.title
            itemView.textSongArtist.text = song.artistName
        }
    }

    interface SongClicked {
        fun onSongClicked(song: Song)
    }
}
