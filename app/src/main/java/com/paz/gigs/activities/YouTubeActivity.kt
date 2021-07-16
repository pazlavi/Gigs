package com.paz.gigs.activities

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.appsflyer.AppsFlyerLib
import com.bumptech.glide.Glide
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.gson.Gson
import com.paz.gigs.BuildConfig
import com.paz.gigs.databinding.ActivityYouTubeBinding
import com.paz.gigs.models.youtubeVideo.YouTubeItem
import com.paz.gigs.utils.Consts
import kotlinx.coroutines.*
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList


class YouTubeActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener,
    YouTubePlayer.PlaybackEventListener {
    companion object {
        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
        const val RECOVERY_DIALOG_REQUEST = 1004
        const val NUMBER_OF_VIDEOS_RETURNED = 100L
        const val TAG = "Paz_YouTubeActivity"


    }

    private lateinit var binding: ActivityYouTubeBinding

    private lateinit var videos: ArrayList<YouTubeItem>
    private var song : YouTubeItem? = null
    private var nextPageToken: String? = null
    private var prevPageToken: String? = null
    private var videoId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYouTubeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent.extras?.let {
          val json =  it.getString(Consts.SONG_KEY)
            song = Gson().fromJson(json , YouTubeItem::class.java)
            Log.d(TAG, "onCreate: $song")
            setVideo()
            setImage()
            setSongTitle()


        }
        setClickListeners()

    }

    private fun setClickListeners() {
        binding.youtubeBARPlayer.setNavigationOnClickListener{
            finish()
        }
    }

    private fun setSongTitle() {
        binding.youtubeLBLTitle.text = song?.snippet?.title
    }


    private fun setImage() {
        val circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        Glide.with(this@YouTubeActivity)
            .load(song?.snippet?.thumbnails?.default?.url)
            .placeholder(circularProgressDrawable)
            .fitCenter()
            .into(binding.youtubeIMGImage)
    }


    private fun setVideo() {
        videoId = song?.id?.videoId.toString()
        if (videoId.isNotEmpty())
            binding.youtubePLYPlayer.initialize(getYouTubeKey(), this@YouTubeActivity)
    }



    private fun getYouTubeKey(): String {
//        val properties = Properties()
//        val assetManager: AssetManager = applicationContext.assets
//        val inputStream: InputStream = assetManager.open("cer.properties")
//        properties.load(inputStream)
//        return properties.getProperty("YOUTUBE_API_KEY")
        return BuildConfig.YOUTUBE_API_KEY
    }





    override fun onInitializationSuccess(
        p0: YouTubePlayer.Provider?,
        youTubePlayer: YouTubePlayer?,
        p2: Boolean
    ) {
        youTubePlayer?.apply {
            setShowFullscreenButton(false)
            setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT)
            loadVideo(videoId)
            setPlaybackEventListener(this@YouTubeActivity)
        }
        AppsFlyerLib.getInstance().logEvent(this, "play_song",
        mapOf("videoID" to videoId , "ts" to System.currentTimeMillis() ))

    }

    override fun onInitializationFailure(
        p0: YouTubePlayer.Provider?,
        p1: YouTubeInitializationResult?
    ) {
        p1?.let {
            if (it.isUserRecoverableError) {
                it.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show()

            }
        }

        Log.d("YoutubeAPI: ", "onInitializationFailure: Failed to init!: ")
    }

    override fun onPlaying() {
    }

    override fun onPaused() {
    }

    override fun onStopped() {
    }

    override fun onBuffering(p0: Boolean) {
    }

    override fun onSeekTo(p0: Int) {
    }


}