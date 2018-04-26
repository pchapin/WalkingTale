package com.talkingwhale.util

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.content.Context
import android.location.Location
import android.support.v7.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.talkingwhale.R

class LocationLiveData(val context: Context) : LiveData<Location>() {

    @SuppressLint("MissingPermission")
    private val fusedLocationClient = FusedLocationProviderClient(context)
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            if (p0 != null) {
                value = p0.lastLocation
            }
        }
    }

    override fun onActive() {
        startLocationUpdates()
    }

    override fun onInactive() {
        stopLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {

        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val locationRequest = LocationRequest()
        val isHighAccuracy = sp.getBoolean(context.resources.getString(R.string.pref_key_location_accuracy), false)

        if (isHighAccuracy) {
            locationRequest.setInterval(10).priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        } else {
            locationRequest.setInterval(30).priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
