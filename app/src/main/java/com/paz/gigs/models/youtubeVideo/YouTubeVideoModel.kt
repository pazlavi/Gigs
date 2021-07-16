package com.paz.gigs.models.youtubeVideo

data class YouTubeVideoModel(
    val youTubeItems: List<YouTubeItem>,
    val nextPageToken: String,
    val prevPageToken: String
)