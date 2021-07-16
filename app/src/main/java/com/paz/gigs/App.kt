package com.paz.gigs

import android.app.Application
import android.content.Intent
import android.content.res.AssetManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.google.android.libraries.places.api.Places
import com.paz.gigs.utils.Consts
import com.paz.prefy_lib.Prefy
import java.io.InputStream
import java.util.*

class App:Application() , DeepLinkListener , AppsFlyerConversionListener{
    companion object{
        const val TAG = "Paz_App"
    }
    override fun onCreate() {
        super.onCreate()
        Prefy.init(this, false)
        Places.initialize(applicationContext,  BuildConfig.MAPS_API_KEY)
        initAppsFlyer()

    }

    private fun initAppsFlyer() {
    AppsFlyerLib.getInstance().apply {
        setDebugLog(BuildConfig.DEBUG)
        init(BuildConfig.APPSFLYER_DEV_KEY  ,this@App , this@App)
        waitForCustomerUserId(true)
        setAppInviteOneLink("mb2K")
        subscribeForDeepLink(this@App)
        start(this@App)
    }
    }

    override fun onDeepLinking(deepLink: DeepLinkResult) {
        Log.d(TAG, "onDeepLinking: $deepLink")
        if (deepLink.status == DeepLinkResult.Status.FOUND){
            Log.d(TAG, "deeplink found: ${deepLink.status}")
            val eventUUID = deepLink.deepLink.deepLinkValue
            val intent = Intent().apply {
                action = Consts.AF_DEEP_LINK
                putExtra(Consts.AF_DEEP_LINK_VALUE , eventUUID)
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

    override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
    }

    override fun onConversionDataFail(p0: String?) {
    }

    override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
    }

    override fun onAttributionFailure(p0: String?) {
    }


}