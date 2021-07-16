package com.paz.gigs.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.paz.gigs.adapters.song_search.SongViewHolder
import com.paz.gigs.adapters.song_search.SongsResultsAdapter
import com.paz.gigs.databinding.FragmentEventSongListBinding
import com.paz.gigs.objects.UserObject
import com.paz.gigs.utils.Consts
import com.paz.gigs.view_models.YouTubeItemsViewModel

class EventSongListFragment() : Fragment() {
    private companion object {
        const val TAG = "Paz_EventSongListFragment"
    }

    private var _binding: FragmentEventSongListBinding? = null
    private lateinit var listener: SongViewHolder.Companion.OnCardClicked
    private val model: YouTubeItemsViewModel by activityViewModels()


    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindPlaylistToList()
        Log.d(TAG, "onViewCreated: ${model.getPlaylist()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SongViewHolder.Companion.OnCardClicked) {
            listener = context

        } else {
            throw ClassCastException(
                "$context must implement SongViewHolder.Companion.OnCardClicked."
            )
        }
    }


    private fun bindPlaylistToList() {

        binding.eventListLSTResults.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }
        model.getPlaylist().observe(viewLifecycleOwner, { lst ->
            Log.d(TAG, "bindPlaylistToList: ${lst.filter { it1 -> it1.isPlayed != true }}")
            binding.eventListLSTResults.adapter = SongsResultsAdapter(
                lst.filter { it1 -> it1.isPlayed != true },
                UserObject.currentUser?.defaultInterface == Consts.DJ_INTERFACE, listener
            )
            (binding.eventListLSTResults.adapter as SongsResultsAdapter).notifyDataSetChanged()
            if (lst.size > 0) {
                binding.eventListLBLQuery.visibility = GONE
            }
            // Update the UI
        })
    }
}
