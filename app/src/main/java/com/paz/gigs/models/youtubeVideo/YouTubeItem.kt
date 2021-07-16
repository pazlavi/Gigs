package com.paz.gigs.models.youtubeVideo

data class YouTubeItem(
    var id: Id?,
    var snippet: Snippet?,
    var likes : MutableList<String>,
    var unlikes :  MutableList<String>,
    var isPlayed : Boolean?
)
{
    constructor() : this(null,null, ArrayList<String>(),ArrayList<String>(),null)
}