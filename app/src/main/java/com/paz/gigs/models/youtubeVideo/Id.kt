package com.paz.gigs.models.youtubeVideo

data class Id(
    val kind: String,
    val videoId: String
){
    constructor():this("","")
}