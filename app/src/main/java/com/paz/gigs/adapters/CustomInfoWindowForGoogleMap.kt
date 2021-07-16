package com.paz.gigs.adapters

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.gson.Gson
import com.paz.gigs.R
import com.paz.gigs.databinding.EventCardBinding
import com.paz.gigs.models.events.EventInfo
import java.text.SimpleDateFormat
import java.util.*

class CustomInfoWindowForGoogleMap(var context: Context) :
    GoogleMap.InfoWindowAdapter {
    companion object {
        const val TAG = "Paz_CustomInfoWindowForGoogleMap"

    }

    var mContext = context
    val binding = EventCardBinding.inflate(LayoutInflater.from(context))

    private fun rendowWindowText(marker: Marker) {
        try {

            bind(Gson().fromJson(marker.snippet, EventInfo::class.java))
        } catch (e: NullPointerException) {

        }

    }

    private fun bind(event: EventInfo) {
        val res = binding.eventCardLBLDateTime.resources
        binding.eventCardLBLDjName.text = res.getString(R.string.djNameWithValue, event.djName)
        binding.eventCardLBLPrName.text = res.getString(R.string.prNameWithValue, event.prName)
        binding.eventCardLBLEventAddress.text =
            res.getString(R.string.addressWithValue, event.address)
        binding.eventCardLBLLocationName.text =
            res.getString(R.string.locationNameWithValue, event.locationName)

        val dt = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(event.eventTimestamp)

        binding.eventCardLBLDateTime.text =
            res.getString(R.string.dateTimeWithValue, dt)
        binding.eventCardLBLGenres.movementMethod = ScrollingMovementMethod()
        binding.eventCardLBLGenres.text =
            res.getString(
                R.string.genresWithValue,
                event.genres.toString().replace("[", "").replace("]", "")
            )



    }


    override fun getInfoContents(marker: Marker): View {
        rendowWindowText(marker)
        return binding.root
    }

    override fun getInfoWindow(marker: Marker): View? {
        rendowWindowText(marker)
        return binding.root
    }
}