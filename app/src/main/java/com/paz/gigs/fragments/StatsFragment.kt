package com.paz.gigs.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.paz.gigs.R
import com.paz.gigs.adapters.top_songs.TopSongsAdapter
import com.paz.gigs.adapters.top_songs.TopSongsViewHolder
import com.paz.gigs.databinding.FragmentStatsBinding
import com.paz.gigs.firestore.DatabaseOperations
import com.paz.gigs.models.youtubeVideo.RatedSong
import com.paz.gigs.models.youtubeVideo.YouTubeItem
import com.paz.gigs.objects.UserObject
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class StatsFragment : Fragment() {
    companion object {
        const val TAG = "Paz_StatsFragment"
    }

    private var _binding: FragmentStatsBinding? = null
    private val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    private var songs: ArrayList<YouTubeItem>? = null
    private var likesCounter: HashMap<String, Int>? = null
    private var ratedSongs: ArrayList<RatedSong>? = null
    private lateinit var callback: TopSongsViewHolder.Companion.OnCardClicked

    private var dateRange = Calendar.getInstance().run {
        val now = this.time.time
        val monthAgo = this.add(Calendar.MONTH, -1).run { time.time }
        Pair(monthAgo, now)
    }

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.statsLSTList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }
        setDateRangeLabel()
        getMyEvents()
        setOnClickListeners()

    }

    private fun getMyEvents() {
        DatabaseOperations.djGetMyEventsInDateRange(
            UserObject.currentUser!!.userUUID,
            dateRange,
            object : DatabaseOperations.OnDataReady {
                override fun onDataReady(res: Any?) {
                    val songsList = res as ArrayList<YouTubeItem>
                    songs = songsList
                    calculateSongsLikes()
                }

            })
    }

    private  fun calculateSongsLikes() {
        GlobalScope.launch(Dispatchers.Default) {
            val songMap = HashMap<String, Int>()
            val songsList = songs
            songsList?.forEach {  s->
                    if (s.id != null) {
                        if (songMap.containsKey(s.id!!.videoId)) {
                            songMap[s.id!!.videoId] = s.likes.size + songMap[s.id!!.videoId]!!
                        } else {
                            songMap[s.id!!.videoId] = s.likes.size
                        }
                    }


                }



            likesCounter = songMap
            withContext(Dispatchers.Main) {
                ratedSongs = ArrayList()
                if(songsList!=null){
                    for ((k, v) in songMap) {
                        ratedSongs!!.add(RatedSong(songsList.first { i -> i.id?.videoId == k }, v))
                    }}
                ratedSongs?.let {
                    binding.statsLSTList.apply {
                        adapter = TopSongsAdapter(
                          it, callback
                        ).apply { notifyDataSetChanged() }

                    }
                }

            }

            Log.d(TAG, "calculateSongsLikes: $songMap")
        }

    }


    private fun setDateRangeLabel() {
        val dtRange =
            "${formatter.format(dateRange.first)} - ${formatter.format(dateRange.second)}"
        binding.statsLBLDt.text = resources.getString(R.string.dateRange, dtRange)
    }

    private fun setOnClickListeners() {
        binding.statsBARTool.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.saved_BTN_favorite -> {
                    showDatePicker()
                    true
                }
                else -> false
            }
        }
    }

    private fun showDatePicker() {


        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates range")
                .setSelection(
                    dateRange
                )
                .build()
        dateRangePicker.addOnPositiveButtonClickListener {
            dateRange = it
            setDateRangeLabel()
            (binding.statsLSTList.adapter as TopSongsAdapter?)?.clearList()
            getMyEvents()

        }
        dateRangePicker.show(childFragmentManager, MaterialTimePicker::class.java.canonicalName)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TopSongsViewHolder.Companion.OnCardClicked) {
            callback = context

        } else {
            throw ClassCastException(
                "$context must implement TopSongsViewHolder.Companion.OnCardClicked."
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}