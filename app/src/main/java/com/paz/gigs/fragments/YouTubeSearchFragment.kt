package com.paz.gigs.fragments

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchResult
import com.google.gson.Gson
import com.paz.gigs.R
import com.paz.gigs.activities.YouTubeActivity
import com.paz.gigs.adapters.song_search.SongViewHolder
import com.paz.gigs.adapters.song_search.SongsResultsAdapter
import com.paz.gigs.databinding.FragmentSearchSongBinding

import com.paz.gigs.models.youtubeVideo.YouTubeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.util.*

class YouTubeSearchFragment : Fragment() {
    private var _binding: FragmentSearchSongBinding? = null
    private lateinit var videos: ArrayList<YouTubeItem>
    private var nextPageToken: String? = null
    private var prevPageToken: String? = null
    private lateinit var listener : SongViewHolder.Companion.OnCardClicked
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videos = ArrayList()
        setOnClick()
    }

    private fun setOnClick() {
        binding.searchBTNNewSearch.setOnClickListener {
            makeRequestTask()
        }
    }



    private fun getSearchText(): String? {
        return binding.youtubeEDTSearch.editText?.text?.toString()
    }
    private fun getYouTubeKey(): String {
        val properties = Properties()
        val assetManager: AssetManager? = activity?.assets
        val inputStream: InputStream? = assetManager?.open("cer.properties")
        properties.load(inputStream)
        return properties.getProperty("YOUTUBE_API_KEY")
    }

    private fun makeRequestTask() {
        binding.youtubeBARProg.visibility = VISIBLE
        binding.youtubeLAYBase.isEnabled = false
        GlobalScope.launch(Dispatchers.IO) {
            try {

                val transport = AndroidHttp.newCompatibleTransport()
                val jsonFactory = GsonFactory.getDefaultInstance()
                val service: YouTube = YouTube.Builder(
                    transport, jsonFactory
                ) { }
                    .setApplicationName("Gigs")
                    .build()
                val search = service.search().list("id,snippet").also {
                    it.key = getYouTubeKey()
                    it.q = getSearchText()
                    it.type = "video"
                    it.maxResults = YouTubeActivity.NUMBER_OF_VIDEOS_RETURNED
                    it.order = "viewCount"
                    it.fields =
                        "items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url),nextPageToken,prevPageToken"
                }
                val searchResponse = search.execute()
                nextPageToken = searchResponse.nextPageToken
                prevPageToken = searchResponse.prevPageToken
                val searchResultList: MutableList<SearchResult>? = searchResponse.items
                withContext(Dispatchers.Default) {
                    videos.clear()
                    searchResultList?.let {
                        val gson = Gson()
                        val itr = it.iterator()
                        while (itr.hasNext()) {
                            val res = itr.next()
                            Log.d("youtubeRes", res.toPrettyString())
                            val model = gson.fromJson(res.toString(), YouTubeItem::class.java)
                            videos.add(model)
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    Log.d(YouTubeActivity.TAG, "videos size = ${videos.size}")
                    bindResultsToList()
                    Log.d(YouTubeActivity.TAG, "fin")
                    binding.youtubeBARProg.visibility = GONE
                    binding.youtubeLAYBase.isEnabled = true




                }

            } catch (e: GoogleJsonResponseException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (t: Throwable) {
                t.printStackTrace()
            }

        }

    }

    private fun bindResultsToList() {
        binding.searchLBLQuery.text = resources.getString(R.string.showResult , getSearchText())
        binding.searchLSTResults.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = SongsResultsAdapter(videos , false,listener)
        }
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

}
