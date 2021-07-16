package com.paz.gigs.helpers

import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint


/** Helper class for google maps LatLng Serializable*/
data class MyLatLng(
    val latitude: Double,
    val longitude: Double
) {
    constructor() : this(-1.0, -1.0)
    constructor(latLng: LatLng) : this(latLng.latitude, latLng.longitude)

    fun toGoogleLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }

    fun  toGeoPoint() : GeoPoint {
        return GeoPoint(latitude ,longitude)
    }
    fun  toGeoLocationHash() : String{
        return GeoFireUtils.getGeoHashForLocation(GeoLocation(latitude, longitude))
    }
}