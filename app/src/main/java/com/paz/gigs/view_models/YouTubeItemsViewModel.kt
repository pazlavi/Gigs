package com.paz.gigs.view_models

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.paz.gigs.models.youtubeVideo.YouTubeItem


class YouTubeItemsViewModel : ViewModel() {
    private val playlist = MutableLiveData<ArrayList<YouTubeItem>>()
    fun setPlaylist(playlist: ArrayList<YouTubeItem>) {
        this.playlist.value = playlist
    }

    fun getPlaylist(): LiveData<ArrayList<YouTubeItem>> {
        return playlist
    }

}