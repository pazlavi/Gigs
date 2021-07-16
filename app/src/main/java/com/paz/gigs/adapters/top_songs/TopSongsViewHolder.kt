package com.paz.gigs.adapters.top_songs

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.paz.gigs.R
import com.paz.gigs.databinding.CardRatedSongBinding
import com.paz.gigs.models.youtubeVideo.RatedSong
import com.paz.gigs.models.youtubeVideo.YouTubeItem

class TopSongsViewHolder(
    private val binding: CardRatedSongBinding,
    private val callback: OnCardClicked
) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        interface OnCardClicked {
            fun playSong(youTubeItem: YouTubeItem)
        }
    }

    fun bind(item: RatedSong, place: Int) {
        Glide.with(binding.ratedIMGImage)
            .load(item.song.snippet!!.thumbnails!!.default!!.url)
            .placeholder(R.drawable.ic_loop)
            .fitCenter()
            .into(binding.ratedIMGImage)
        binding.ratedLBLTitle.text = item.song.snippet!!.title

        binding.ratedLAYCard.setOnClickListener {
            callback.playSong(item.song)
        }
        binding.ratedLBLPlace.text =
            binding.ratedLBLPlace.resources.getString(R.string.placeNumber, place)

        binding.ratedLBLLikes.text =
            binding.ratedLBLLikes.resources.getString(R.string.totalLikes, item.likes)

    }


}