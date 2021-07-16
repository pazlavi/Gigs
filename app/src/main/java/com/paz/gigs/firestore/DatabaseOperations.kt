package com.paz.gigs.firestore

import android.util.Log
import androidx.core.util.Pair
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.paz.gigs.models.enums.UserType
import com.paz.gigs.models.events.EventInfo
import com.paz.gigs.models.users.DjUser
import com.paz.gigs.models.users.User
import com.paz.gigs.models.youtubeVideo.YouTubeItem
import com.paz.gigs.utils.Consts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


object DatabaseOperations {
    private const val TAG = "Paz_DatabaseOperations"

    interface OnDataReady {
        fun onDataReady(res: Any?)
    }

    interface OnInsertResults {
        fun isInserted(insert: Boolean)
    }

    fun getAllPrUsers(call: OnDataReady) {
        GlobalScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore
            val query = db.collection("users").document("all_users")
                .collection("all_users_list")
                .whereArrayContains("userTypes", UserType.PR.toString()).orderBy("fullName")
            query.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    GlobalScope.launch(Dispatchers.Default) {
                        val users = ArrayList<User>()
                        it.result?.let { docs ->
                            for (doc in docs.documents) {
                                doc?.let { d ->
                                    users.add(d.toObject(User::class.java)!!)
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            call.onDataReady(users)
                        }
                    }


                } else {
                    it.exception?.printStackTrace()
                    call.onDataReady(null)
                }


            }
        }
    }

    fun getAllDjUsers(call: OnDataReady) {
        GlobalScope.launch(Dispatchers.IO) {


            val db = Firebase.firestore
            val query = db.collection("users")
                .document("djs")
                .collection("djs_list")
                .orderBy("stageName")
            query.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    GlobalScope.launch(Dispatchers.Default) {
                        val users = ArrayList<DjUser>()
                        it.result?.let { docs ->
                            for (doc in docs.documents) {
                                doc?.let { d ->
                                    users.add(d.toObject(DjUser::class.java)!!)
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            call.onDataReady(users)
                        }
                    }


                } else {
                    it.exception?.printStackTrace()
                    call.onDataReady(null)
                }


            }
        }
    }

    fun saveNewEvent(event: EventInfo, listener: OnInsertResults) {
        GlobalScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore
            db.collection("events").document(event.eventUUID).set(event)
                .addOnCompleteListener { t ->
                    GlobalScope.launch(Dispatchers.Main) {
                        run {
                            listener.isInserted(t.isSuccessful)
                        }
                    }
                }
        }
    }

    fun getMyFutureEvents(userId: String, isDj: Boolean, call: OnDataReady) {
        GlobalScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore
            db.collection("events").whereEqualTo((if (isDj) "djUUID" else "prUUID"), userId)
                .whereGreaterThan("eventTimestamp", System.currentTimeMillis())
                .orderBy("eventTimestamp", Query.Direction.ASCENDING)
                .get().addOnCompleteListener { t ->
                    GlobalScope.launch(Dispatchers.Default) {
                        run {
                            val data = ArrayList<EventInfo>()
                            if (t.isSuccessful) {
                                t.result.documents.forEach { d ->
                                    data.add(d.toObject(EventInfo::class.java)!!)
                                }
                            }
                            withContext(Dispatchers.Main) {
                                call.onDataReady(data)
                            }
                        }
                    }
                }
        }
    }

    fun djGetMyEventsInDateRange(userId: String, dateRange: Pair<Long, Long>, call: OnDataReady) {
        GlobalScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore
            db.collection("events").whereEqualTo("djUUID", userId)
                .whereGreaterThanOrEqualTo("eventTimestamp", dateRange.first)
                .whereLessThanOrEqualTo("eventTimestamp", dateRange.second)
                .orderBy("eventTimestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener { t ->
                    GlobalScope.launch(Dispatchers.Default) {
                        run {
                            val data = ArrayList<YouTubeItem>()
                            if (t.isSuccessful) {
                                t.result.documents.forEach { d ->
                                    withContext(Dispatchers.IO) {
                                        d.reference.collection("songs").get()
                                            .addOnCompleteListener {

                                                GlobalScope.launch(Dispatchers.Default) {
                                                    if (it.isSuccessful) {
                                                        it.result.documents.forEach { s ->
                                                            data.add(s.toObject(YouTubeItem::class.java)!!)
                                                        }

                                                    }
                                                    withContext(Dispatchers.Main) {
                                                        call.onDataReady(data)
                                                    }
                                                }
                                            }
                                    }
                                }
                            }

                        }
                    }
                }
        }
    }

    fun searchForEvents(
        lat: Double,
        lan: Double,
        radiusInK: Int,
        dtRage: Pair<Long, Long>,
        genres: ArrayList<String>,
        call: OnDataReady
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore
            val center = GeoLocation(lat, lan)
            val radiusInM = (radiusInK * 1000).toDouble()
            val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
            val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()

            for (b in bounds) {

                val q: Query = db.collection("events")
//                    .whereGreaterThan("eventTimestamp", System.currentTimeMillis())
//                    .orderBy("eventTimestamp", Query.Direction.ASCENDING)
                    .orderBy("geoHash")
                    .startAt(b.startHash)
                    .endAt(b.endHash)
                tasks.add(q.get())
            }

            // Collect all the query results together into a single list
            // Collect all the query results together into a single list
            Tasks.whenAllComplete(tasks)
                .addOnCompleteListener {
                    GlobalScope.launch(Dispatchers.Default) {

                        val events: MutableList<EventInfo> = ArrayList()
                        for (task in tasks) {
                            val snap = task.result
                            for (doc in snap.documents) {
                                val e = doc.toObject(EventInfo::class.java)!!
                                val lst = if (genres.isEmpty())
                                    ArrayList<String>().apply {
                                        Consts.MUSIC_GENRES_MAP.values.forEach { arr -> addAll(arr) }
                                    }
                                else genres

                                // We have to filter out a few false positives due to GeoHash
                                // accuracy, but most will match
                                val docLocation =
                                    GeoLocation(e.latLng!!.latitude, e.latLng.longitude)
                                val distanceInM =
                                    GeoFireUtils.getDistanceBetween(docLocation, center)
                                if (distanceInM <= radiusInM && e.eventTimestamp > dtRage.first && e.eventTimestamp < dtRage.second &&
                                    e.genres?.any { it in lst } == true && !e.isPrivateEvent
                                ) {
                                    events.add(e)
                                }
                            }
                        }

                        // events contains the results
                        withContext(Dispatchers.Main) {
                            call.onDataReady(events)
                        }
                    }

                }
        }
    }

    fun saveSongToEvent(event: EventInfo, song: YouTubeItem) {
        GlobalScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore
            db.collection("events").document(event.eventUUID)
                .collection("songs").document(song.id!!.videoId).set(song)
        }
    }

    fun getEventPlaylist(event: EventInfo, call: OnDataReady) {
        GlobalScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore
            db.collection("events").document(event.eventUUID)
                .collection("songs")
//                .whereNotEqualTo("isPlayed", true)
//                .orderBy("isPlayed", Query.Direction.ASCENDING)
                .orderBy("likes", Query.Direction.DESCENDING)
                .orderBy("unlikes", Query.Direction.ASCENDING).get().addOnCompleteListener { t ->
                    GlobalScope.launch(Dispatchers.Main) {
                        run {
                            val data = ArrayList<YouTubeItem>()
                            if (t.isSuccessful) {
                                t.result.documents.forEach { d ->
                                    data.add(d.toObject(YouTubeItem::class.java)!!)
                                }
                            }
                            call.onDataReady(data)
                        }
                    }
                }
        }
    }

    fun updateSongPlayedStatus(event: EventInfo, youTubeItem: YouTubeItem) {
        GlobalScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore
            db.collection("events").document(event.eventUUID)
                .collection("songs").document(youTubeItem.id!!.videoId).update("played", true)
        }
    }


    fun addEventToFavorite(event: EventInfo, userUUID: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore
            db.collection("favorites_events").document(userUUID)
                .update(Consts.SAVED_EVENTS_KEY, FieldValue.arrayUnion(event.eventUUID))
        }
    }

    fun getFavoriteEvents(userUUID: String, call: OnDataReady) {
        GlobalScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore
            db.collection("favorites_events").document(userUUID).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val eventList = it.result.get(Consts.SAVED_EVENTS_KEY) as List<String>
                    Log.d(TAG, "getFavoriteEvents: $eventList")
                    db.collection("events").whereIn("eventUUID", eventList).get()
                        .addOnCompleteListener { res ->
                            if (res.isSuccessful) {
                                GlobalScope.launch(Dispatchers.Main) {
                                    call.onDataReady(res.result.documents.map { d ->
                                        d.toObject(
                                            EventInfo::class.java
                                        )
                                    })
                                }
                            }
                        }
                }
            }
        }
    }

    fun getEventById(eventUUID: String, call: OnDataReady) {
        GlobalScope.launch(Dispatchers.IO){
            val db = Firebase.firestore
            db.collection("events").document(eventUUID).get().addOnCompleteListener {
                if (it.isSuccessful){
                    val event = it.result.toObject(EventInfo::class.java)!!
                    GlobalScope.launch(Dispatchers.Main) {
                        call.onDataReady(event)}
                }
            }
        }
    }


}