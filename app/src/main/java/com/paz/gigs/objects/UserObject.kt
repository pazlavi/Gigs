package com.paz.gigs.objects

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.paz.gigs.models.enums.UserType
import com.paz.gigs.models.users.User
import com.paz.gigs.utils.Consts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object UserObject {
    var currentUser: User? = null
    var used = false
    fun findUser(uuid: String, callback: OnFindUserListener) {
        Firebase.firestore.collection("users").document("all_users").collection("all_users_list")
            .document(uuid).get().addOnCompleteListener { t ->
                if (t.isSuccessful) {
                    val doc = t.result
                    currentUser = doc?.toObject(User::class.java)
                }
                used = true
                callback.getResults(t.isSuccessful, currentUser)
            }
    }


    fun updateDefaultInterface(n: Int) {

        GlobalScope.launch(Dispatchers.IO) {
            currentUser?.let {
                Firebase.firestore.collection("users").document("all_users")
                    .collection("all_users_list")
                    .document(it.userUUID).update("defaultInterface", n)
                it.defaultInterface = n
                if(it.userTypes.contains(UserType.DJ)){
                    DjUserObject.updateDefaultInterface(n ,it.userUUID)
                }
            }
        }
    }

    interface OnFindUserListener {
        fun getResults(isSuccessful: Boolean, user: User?)
    }

}