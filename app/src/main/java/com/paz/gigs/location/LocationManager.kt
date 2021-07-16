package com.paz.gigs.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.paz.gigs.utils.Consts


class LocationManager : Service() {
    private companion object {

        private const val TIME_INTERVAL = 5000L
        private const val FASTEST_TIME_INTERVAL = 1000L
        private const val MAX_WAIT = 1000L
        private const val TAG = "Paz_LocationManager"
    }


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null

    private lateinit var context: Application

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        buildLocationRequest()
        buildLocationCallBack()
        subscribeToLocation()
        unsubscribeToLocation()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun unsubscribeToLocation() {
        val removeTask = fusedLocationClient.removeLocationUpdates(locationCallback)
        removeTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Location Callback removed.")
                stopSelf()
            } else {
                Log.d(TAG, "Failed to remove Location Callback.")
            }
        }
    }

    private fun subscribeToLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = TIME_INTERVAL
            fastestInterval = FASTEST_TIME_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
            isWaitForAccurateLocation = true

        }
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.lastLocation
                currentLocation?.let { location ->
                    val intent = Intent().apply {
                        action = Consts.LAST_LOCATION
                        putExtra(Consts.LAT, location.latitude)
                        putExtra(Consts.LNG, location.longitude)
                    }
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                }

//                if (serviceRunningInForeground) {
//                    notificationManager.notify(
//                        NOTIFICATION_ID,
//                        generateNotification(currentLocation)
//                    )
//                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "lastLocation:${it.result} ")
                val intent = Intent().apply {
                    action = Consts.LAST_LOCATION
                    putExtra(Consts.LAT, it.result.latitude)
                    putExtra(Consts.LNG, it.result.longitude)
                }
                context.sendBroadcast(intent)
            } else {
                Log.d(TAG, "lastLocation: error ${it.exception?.message}")
                it.exception?.printStackTrace()

            }
        }


    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }


}
