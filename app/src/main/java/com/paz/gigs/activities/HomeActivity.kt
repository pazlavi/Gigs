package com.paz.gigs.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.appsflyer.AppsFlyerLib
import com.firebase.ui.auth.AuthUI
import com.google.gson.Gson
import com.paz.gigs.R
import com.paz.gigs.adapters.top_songs.TopSongsViewHolder
import com.paz.gigs.callbacks.EventSelectedListener
import com.paz.gigs.databinding.ActivityHomeBinding
import com.paz.gigs.firestore.DatabaseOperations
import com.paz.gigs.fragments.UserProfileFragment
import com.paz.gigs.models.events.EventInfo
import com.paz.gigs.models.users.DjUser
import com.paz.gigs.models.youtubeVideo.YouTubeItem
import com.paz.gigs.objects.DjUserObject
import com.paz.gigs.objects.UserObject
import com.paz.gigs.utils.Consts
import com.paz.gigs.view_models.UsersViewModel


class HomeActivity : AppCompatActivity(), EventSelectedListener,
    UserProfileFragment.Companion.LogoutListener, TopSongsViewHolder.Companion.OnCardClicked {
    private companion object {
        const val TAG = "Paz_DjActivity"
    }

    private lateinit var binding: ActivityHomeBinding
    private var currentId: Int? = null
    private val model: UsersViewModel by viewModels()

    private val listener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive: ")
            if (intent != null && intent.action == Consts.AF_DEEP_LINK) {
                val eventId = intent.getStringExtra(Consts.AF_DEEP_LINK_VALUE)
                Log.d(TAG, "onReceive: eventId = $eventId")
                handleDeepLink(eventId)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(listener, IntentFilter(Consts.AF_DEEP_LINK))
        checkForDeepLinkFromPreviousActivity()
    }

    private fun checkForDeepLinkFromPreviousActivity() {
        val eventId = intent.getStringExtra(Consts.AF_DEEP_LINK_VALUE)
        intent.removeExtra(Consts.AF_DEEP_LINK_VALUE)
        Log.d(TAG, "checkForDeepLinkFromPreviousActivity: eventId = $eventId")
        handleDeepLink(eventId)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            this.intent = it
        }
    }
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(listener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (UserObject.currentUser != null) {
            model.setUser(UserObject.currentUser!!)
            if (UserObject.currentUser!!.defaultInterface == Consts.DJ_INTERFACE) {
                DjUserObject.findUser(
                    UserObject.currentUser!!.userUUID,
                    object : DjUserObject.OnFindUserListener {
                        override fun getResults(isSuccessful: Boolean, user: DjUser?) {
                            if (isSuccessful && user != null) {
                                model.setDj(user)
                            }
                        }

                    })
            }
        }
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val showBar =
            arrayListOf(
                R.id.statsFragment,
                R.id.djProfileFragment,
                R.id.eventsMapsFragment,
                R.id.savedEventsFragment
            )
        val navController = findNavController(R.id.dj_FRG_content)
        binding.bottomNavigation.setupWithNavController(navController)
        when (UserObject.currentUser?.defaultInterface) {
            Consts.DJ_INTERFACE -> binding.bottomNavigation.menu.findItem(R.id.statsFragment).isVisible =
                true
            Consts.CLUBBER_INTERFACE -> binding.bottomNavigation.menu.findItem(R.id.savedEventsFragment).isVisible =
                true
        }
        navController.addOnDestinationChangedListener { _, des, _ ->
            currentId = des.id
            Log.d(TAG, "des = ${des.id}")
            if (showBar.contains(des.id)) {
                binding.bottomNavigation.visibility = VISIBLE
            } else {
                binding.bottomNavigation.visibility = GONE

            }
        }
    }


    private fun handleDeepLink(eventId: String?) {
        Log.d(TAG, "handleDeepLink: ")
        if (!eventId.isNullOrEmpty()) {
            DatabaseOperations.getEventById(eventId, object : DatabaseOperations.OnDataReady {
                override fun onDataReady(res: Any?) {
                    if (res != null) {
                        val event = res as EventInfo
                        onEventSelected(event)
                    }
                }
            })
        }
    }


    override fun onBackPressed() {
        if (currentId == R.id.selectGenresFragment) {
            Toast.makeText(this, "Please use the save button!", Toast.LENGTH_SHORT).show()
            return
        }
        super.onBackPressed()


    }

    override fun onEventSelected(event: EventInfo) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(Consts.EVENT_KEY, Gson().toJson(event))
        }
        AppsFlyerLib.getInstance().logEvent(this,"event_selected",
        mapOf("eventUUID" to event.eventUUID , "dj_stage_name" to event.djStageName))
        startActivity(intent)
    }

    override fun logout() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener { t ->
                if (t.isSuccessful) {
                    val i = Intent(this, LoginActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(i)
                    finish()
                }
            }

    }

    override fun playSong(youTubeItem: YouTubeItem) {
        val intent = Intent(this, YouTubeActivity::class.java).apply {
            putExtra(Consts.SONG_KEY, Gson().toJson(youTubeItem))
        }
        startActivity(intent)
    }

}