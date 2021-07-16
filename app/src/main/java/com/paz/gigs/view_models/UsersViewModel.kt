package com.paz.gigs.view_models

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.paz.gigs.models.events.EventInfo
import com.paz.gigs.models.users.DjUser
import com.paz.gigs.models.users.User
import com.paz.gigs.models.youtubeVideo.YouTubeItem


class UsersViewModel : ViewModel() {
    private val dj = MutableLiveData<DjUser>()
    private val user = MutableLiveData<User>()
    fun setDj(dj: DjUser) {
        this.dj.value = dj
    }

    fun getDj(): LiveData<DjUser> {
        return dj
    }

    fun setUser(user: User) {
        this.user.value = user
    }

    fun getUser(): LiveData<User> {
        return user
    }
}