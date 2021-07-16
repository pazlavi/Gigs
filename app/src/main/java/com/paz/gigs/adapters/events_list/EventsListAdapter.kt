package com.paz.gigs.adapters.events_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.paz.gigs.databinding.EventCardBinding
import com.paz.gigs.models.events.EventInfo
import androidx.core.util.Pair


class EventsListAdapter(private var events: ArrayList<EventInfo>, private val callback : EventViewHolder.Companion.OnCardClicked) :
    RecyclerView.Adapter<EventViewHolder>() {
    private var filteredEvents : ArrayList<EventInfo> = events

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding =
            EventCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding,callback)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(filteredEvents[position])
    }

    override fun getItemCount(): Int {
        return filteredEvents.size
    }
fun filterByDate(dateRange : Pair<Long,Long>){
    filteredEvents = ArrayList(events.filter { e -> e.eventTimestamp >= dateRange.first && e.eventTimestamp <= dateRange.second })
    notifyDataSetChanged()
}
}