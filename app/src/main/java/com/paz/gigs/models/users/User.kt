package com.paz.gigs.models.users

import com.paz.gigs.models.enums.UserType

data class User(
    override var userUUID: String,
    override var fullName: String,
    override var phoneNumber: String,
    override var email: String,
    override var userTypes: List<UserType>,
    override var defaultInterface: Int

) : UserAbs {
    constructor() : this("", "", "", "", ArrayList<UserType>(),-1)

}