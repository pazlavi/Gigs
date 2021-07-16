package com.paz.gigs.models.users

import com.paz.gigs.models.enums.UserType

data class DjUser(
    override var userUUID: String,
    override var fullName: String,
    override var phoneNumber: String,
    override var email: String,
    override var userTypes: List<UserType>,
    override  var defaultInterface : Int,
    var stageName:String,
    var genresSet: List<String>
) : UserAbs {
    fun toUser() : User{
        return User(userUUID, fullName, phoneNumber, email, userTypes,-1)
    }

    constructor() : this("", "", "", "", ArrayList<UserType>(),-1,"",ArrayList<String>())
}