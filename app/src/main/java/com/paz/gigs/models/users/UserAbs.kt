package com.paz.gigs.models.users

import com.paz.gigs.models.enums.UserType

interface UserAbs {
     var userUUID: String
     var fullName: String
     var phoneNumber: String
     var email: String
     var userTypes: List<UserType>
     var defaultInterface : Int
}