package com.paz.gigs.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.RectangularBounds
import java.io.IOException
import java.util.*
import kotlin.math.cos


object LocationUtils {
    private const val TAG = "Paz_LocationUtils"
    fun getLocationFromAddress(context: Context?, strAddress: String?): LatLng? {
        val coder = Geocoder(context)
        val address: List<Address>?
        var p1: LatLng? = null
        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return null
            }
            val location: Address = address[0]
            p1 = LatLng(location.latitude, location.longitude)
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
        return p1
    }

    fun getAddressFromLocation(context: Context, latLng: LatLng) {
        val addresses: List<Address>
        val coder: Geocoder = Geocoder(context, Locale.getDefault())

        addresses = coder.getFromLocation(
            latLng.latitude,
            latLng.longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


        val address =
            addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        Log.d(TAG, "getAddressFromLocation: address = $address")


        val city = addresses[0].locality
        val state = addresses[0].adminArea
        val country = addresses[0].countryName
        val postalCode = addresses[0].postalCode
        val knownName = addresses[0].featureName // Only if available else return NULL
        Log.d(TAG, "getAddressFromLocation: city = $city")
        Log.d(TAG, "getAddressFromLocation: state = $state")
        Log.d(TAG, "getAddressFromLocation: country = $country")
        Log.d(TAG, "getAddressFromLocation: postalCode = $postalCode")
        Log.d(TAG, "getAddressFromLocation: knownName = $knownName")

    }

    /**
     * @param radius - in meters
     */
    fun getRectangularBoundsWithRadius(latLng: LatLng , radius :  Long) : RectangularBounds{
        return RectangularBounds.newInstance(
            getCoordinate(latLng.latitude, latLng.longitude, -radius, -radius),
            getCoordinate(latLng.latitude, latLng.longitude, radius, radius))

    }
    private  fun getCoordinate(lat0: Double, lng0: Double, dy: Long, dx: Long): LatLng {
        val lat = lat0 + 180 / Math.PI * (dy / 6378137)
        val lng = lng0 + 180 / Math.PI * (dx / 6378137) / cos(lat0)
        return LatLng(lat, lng)
    }
}