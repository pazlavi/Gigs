package com.paz.gigs.adapters.top_songs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.paz.gigs.databinding.CardRatedSongBinding
import com.paz.gigs.databinding.CardYoutubeResultBinding
import com.paz.gigs.models.youtubeVideo.RatedSong
import com.paz.gigs.models.youtubeVideo.YouTubeItem

class TopSongsAdapter(private var songs: MutableList<RatedSong>, private val callback: TopSongsViewHolder.Companion.OnCardClicked) :
    RecyclerView.Adapter<TopSongsViewHolder>() {
    init {
        songs.sort()
        songs.reverse()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopSongsViewHolder {
        val binding =
            CardRatedSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopSongsViewHolder(binding,callback)
    }

    override fun onBindViewHolder(holder: TopSongsViewHolder, position: Int) {
        holder.bind(songs[position] , position+1)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun clearList(){
        songs.clear()
        notifyDataSetChanged()
    }


}