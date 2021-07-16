package com.paz.gigs.view_models

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.paz.gigs.models.events.EventInfo
import com.paz.gigs.models.youtubeVideo.YouTubeItem


class EventInfoViewModel : ViewModel() {
    private val event = MutableLiveData<EventInfo>()
    fun setEvent(event: EventInfo) {
        this.event.value = event
    }

    fun getEvent(): LiveData<EventInfo> {
        return event
    }

}