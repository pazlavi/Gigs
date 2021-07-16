package com.paz.gigs.models.youtubeVideo

data class RatedSong(val song : YouTubeItem, val likes : Int) : Comparable<RatedSong> {
    override fun compareTo(other: RatedSong): Int {
        return this.likes - other.likes
    }
}