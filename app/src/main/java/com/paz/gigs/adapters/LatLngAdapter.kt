package com.paz.gigs.adapters

import com.google.android.gms.maps.model.LatLng
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

/** Handle conversion from varying types of latitude and longitude representations.  */
class LatLngAdapter : TypeAdapter<LatLng?>() {
    @Throws(IOException::class)
    override fun read(reader: JsonReader): LatLng? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        var lat = 0.0
        var lng = 0.0
        var hasLat = false
        var hasLng = false
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if ("lat" == name || "latitude" == name) {
                lat = reader.nextDouble()
                hasLat = true
            } else if ("lng" == name || "longitude" == name) {
                lng = reader.nextDouble()
                hasLng = true
            }
        }
        reader.endObject()
        return if (hasLat && hasLng) {
            LatLng(lat, lng)
        } else {
            null
        }
    }

    /** Not supported.  */
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: LatLng?) {
        throw UnsupportedOperationException("Unimplemented method.")
    }
}