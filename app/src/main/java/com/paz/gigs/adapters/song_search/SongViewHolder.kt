package com.paz.gigs.adapters.song_search

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.paz.gigs.R
import com.paz.gigs.databinding.CardYoutubeResultBinding
import com.paz.gigs.models.youtubeVideo.YouTubeItem
import com.paz.gigs.objects.UserObject
import com.paz.gigs.utils.Consts

class SongViewHolder(
    private val binding: CardYoutubeResultBinding,
    private val callback: OnCardClicked, private val isDjUser: Boolean,
) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        interface OnCardClicked {
            fun playSong(youTubeItem: YouTubeItem)
            fun likeSong(youTubeItem: YouTubeItem)
            fun unlikeSong(youTubeItem: YouTubeItem)
            fun markAsPlayed(youTubeItem: YouTubeItem)
        }
    }

    fun bind(youTubeItem: YouTubeItem) {
        Glide.with(binding.rescardIMGImage)
            .load(youTubeItem.snippet!!.thumbnails!!.default!!.url)
            .placeholder(R.drawable.ic_loop)
            .fitCenter()
            .into(binding.rescardIMGImage)
        binding.rescardLBLTitle.text = youTubeItem.snippet!!.title
        binding.rescardBTNLike.setOnClickListener {
            callback.likeSong(youTubeItem)
        }
        binding.rescardBTNUnlike.setOnClickListener {
            callback.unlikeSong(youTubeItem)
        }
        binding.rescardLAYCard.setOnClickListener {
            callback.playSong(youTubeItem)
        }

        val likes = youTubeItem.likes.size
        val unlikes = youTubeItem.unlikes.size
        if (likes > 0) {
            binding.rescardLBLLikes.text =
                binding.rescardLBLLikes.resources.getString(R.string.totalLikes, likes)
            binding.rescardLBLLikes.visibility = VISIBLE
        }
        if (unlikes > 0) {
            binding.rescardLBLUnlikes.text =
                binding.rescardLBLUnlikes.resources.getString(R.string.totalLikes, likes)
            binding.rescardLBLUnlikes.visibility = VISIBLE
        }
        if (isDjUser) {
            binding.rescardBTNPlayed.apply {
                setOnClickListener {
                    callback.markAsPlayed(youTubeItem)
                }
                visibility = VISIBLE
            }
        }
    }



}