package com.paz.gigs.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.gson.Gson
import com.paz.gigs.R
import com.paz.gigs.adapters.song_search.SongViewHolder
import com.paz.gigs.databinding.ActivityMainBinding
import com.paz.gigs.firestore.DatabaseOperations
import com.paz.gigs.models.events.EventInfo
import com.paz.gigs.models.youtubeVideo.YouTubeItem
import com.paz.gigs.objects.UserObject
import com.paz.gigs.utils.Consts
import com.paz.gigs.view_models.EventInfoViewModel
import com.paz.gigs.view_models.YouTubeItemsViewModel


class MainActivity : AppCompatActivity(), SongViewHolder.Companion.OnCardClicked {
    companion object {
        const val TAG = "Paz_MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private var event: EventInfo? = null
    private val model: YouTubeItemsViewModel by viewModels()
    private val eventModel: EventInfoViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getEventData()
        val navController = findNavController(R.id.main_FRG_content)
        binding.bottomNavigation.setupWithNavController(navController)


    }

    private fun getEventData() {
        intent?.getStringExtra(Consts.EVENT_KEY)?.let {
            event = Gson().fromJson(it, EventInfo::class.java)
            if (event != null)
                eventModel.setEvent(event!!)
            Log.d(TAG, "getEventData: eventInfo = $event")
            updateEventData()
        }
    }

    private fun updateEventData() {
        DatabaseOperations.getEventPlaylist(event!!, object : DatabaseOperations.OnDataReady {
            override fun onDataReady(res: Any?) {
                if (res != null) {
                    val data = res as ArrayList<YouTubeItem>
                    model.setPlaylist(data)
                    Log.d(TAG, "getEventData onDataReady: $data")
                }
            }

        })

    }

    override fun playSong(youTubeItem: YouTubeItem) {
        val intent = Intent(this, YouTubeActivity::class.java).apply {
            putExtra(Consts.SONG_KEY, Gson().toJson(youTubeItem))
        }
        startActivity(intent)
    }

    override fun likeSong(youTubeItem: YouTubeItem) {
        Log.d(TAG, "likeSong: $youTubeItem")
        // if already liked the song -> don't do anything
        youTubeItem.likes.let {
            UserObject.currentUser?.userUUID?.let { uid ->
                if (it.contains(uid)) {
                    return
                } else {
                    it.add(uid)
                }
            }
        }

        // remove from unlikes uf needed
        youTubeItem.unlikes.let {
            UserObject.currentUser?.userUUID?.let { uid ->
                if (it.contains(uid)) {
                    it.remove(uid)
                }
            }
        }
        DatabaseOperations.saveSongToEvent(event!!, youTubeItem)
        updateEventData()

    }

    override fun unlikeSong(youTubeItem: YouTubeItem) {
        Log.d(TAG, "unlikeSong: $youTubeItem")
        youTubeItem.unlikes.let {
            UserObject.currentUser?.userUUID?.let { uid ->
                if (it.contains(uid)) {
                    return
                } else {
                    it.add(uid)
                }
            }
        }

        // remove from likes if needed
        youTubeItem.likes.let {
            UserObject.currentUser?.userUUID?.let { uid ->
                if (it.contains(uid)) {
                    it.remove(uid)
                }
            }
        }
        DatabaseOperations.saveSongToEvent(event!!, youTubeItem)
        updateEventData()
    }

    override fun markAsPlayed(youTubeItem: YouTubeItem) {
        DatabaseOperations.updateSongPlayedStatus(event!!, youTubeItem)
        updateEventData()
    }


}

