package com.paz.gigs.callbacks

import com.paz.gigs.models.users.User

interface OnRegistration {
    fun  onRegistered(user : User)

}