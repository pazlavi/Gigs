package com.paz.gigs.objects

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.paz.gigs.models.users.DjUser
import com.paz.gigs.models.users.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object DjUserObject {
    var currentUser: DjUser? = null
    var used = false
    fun findUser(uuid: String, callback : OnFindUserListener) {
        Firebase.firestore.collection("users") .document("djs")
            .collection("djs_list")
            .document(uuid).get().addOnCompleteListener { t ->
                if (t.isSuccessful) {
                    val doc = t.result
                    currentUser = doc?.toObject(DjUser::class.java)
                }
                used = true
                callback.getResults(t.isSuccessful , currentUser)
            }
    }


    fun updateDefaultInterface(n: Int , userUUID : String) {
        GlobalScope.launch(Dispatchers.IO) {
            UserObject.currentUser?.let {
                Firebase.firestore.collection("users") .document("djs")
                    .collection("djs_list")
                    .document(userUUID).update("defaultInterface", n)
                it.defaultInterface = n

            }
        }
    }

interface OnFindUserListener{
    fun getResults(isSuccessful:Boolean, user: DjUser? )
}

}