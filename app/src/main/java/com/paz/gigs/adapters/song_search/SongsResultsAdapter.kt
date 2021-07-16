package com.paz.gigs.adapters.song_search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.paz.gigs.databinding.CardYoutubeResultBinding
import com.paz.gigs.models.youtubeVideo.YouTubeItem

class SongsResultsAdapter(private val songs: List<YouTubeItem>, private val isDjUser: Boolean, private val callback: SongViewHolder.Companion.OnCardClicked) :
    RecyclerView.Adapter<SongViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding =
            CardYoutubeResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding,callback,isDjUser)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int {
        return songs.size
    }


}