package com.paz.gigs.adapters.events_list

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.paz.gigs.R
import com.paz.gigs.databinding.EventCardBinding
import com.paz.gigs.models.events.EventInfo
import java.text.SimpleDateFormat
import java.util.*

class EventViewHolder(
    private val binding: EventCardBinding,
    private val callback: OnCardClicked
) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val TAG = "Paz_EventViewHolder"

        interface OnCardClicked {
            fun cardClicked(event: EventInfo)
        }
    }

    fun bind(event: EventInfo) {
        val res = binding.eventCardLBLDateTime.resources
        binding.eventCardLBLDjName.text =         res.getString(R.string.djNameWithValue,event.djName)
        binding.eventCardLBLPrName.text =         res.getString(R.string.prNameWithValue,event.prName)
        binding.eventCardLBLEventAddress.text =   res.getString(R.string.addressWithValue,event.address)
        binding.eventCardLBLLocationName.text =   res.getString(R.string.locationNameWithValue,event.locationName)

        val dt = SimpleDateFormat("dd-MM-yyyy HH:mm" , Locale.US).format(event.eventTimestamp)

        Log.d(TAG, "bind: ")

        binding.eventCardLBLDateTime.text =
            res.getString(R.string.dateTimeWithValue, dt)
        binding.eventCardLBLGenres.text =
            res.getString(R.string.genresWithValue, event.genres.toString().replace("[", "").replace("]", ""))
        binding.eventCardLAYCard.setOnClickListener {
            callback.cardClicked(event)
        }

    }


}