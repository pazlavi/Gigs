package com.paz.gigs.callbacks

import com.paz.gigs.models.events.EventInfo

interface EventSelectedListener {
    fun onEventSelected(event: EventInfo)
}