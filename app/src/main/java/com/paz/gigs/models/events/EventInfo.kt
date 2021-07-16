package com.paz.gigs.models.events

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.paz.gigs.helpers.MyLatLng
import java.util.*

data class EventInfo(
    val eventUUID: String,
    val djName: String,
    val djStageName: String,
    val prName: String,
    val locationName: String,
    val address: String,
    val latLng: MyLatLng?,
    val geoHash : String?,
    val eventTimestamp: Long,
    val creationTimestamp: Long,
    val genres: List<String>?,
    val isPrivateEvent: Boolean,
    val djUUID: String?,
    val prUUID: String?
) {
    constructor() : this("", "", "", "", "", "", null,null, 0L, 0L, null, false,
    null,null)

    fun toJson() : String{
        return Gson().toJson(this)
    }
}