package com.talkingwhale.util

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

class LocationLiveData(context: Context) : LiveData<Location>() {

    @SuppressLint("MissingPermission")
    private val fusedLocationClient = FusedLocationProviderClient(context)
    private val locationRequest = LocationRequest().setInterval(10).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            if (p0 != null)
                value = p0.lastLocation
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
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
