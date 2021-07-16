package com.paz.gigs.models.youtubeVideo

data class Snippet(
    val thumbnails: Thumbnails?,
    val title: String
){
    constructor():this(null,"")
}