package com.paz.gigs.helpers

import android.os.Parcel
import android.os.Parcelable
import com.google.android.libraries.places.api.model.AutocompletePrediction

class AutocompletePredictionWrapper(private val parcel: AutocompletePrediction)  {

    fun getAutocompletePrediction(): AutocompletePrediction {
        return parcel
    }
    override fun toString(): String {
        return parcel.toString()
    }
}