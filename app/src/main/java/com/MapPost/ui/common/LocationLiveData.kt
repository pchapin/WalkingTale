package com.MapPost.ui.common

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.content.Context
import android.location.Location
import android.os.Bundle

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class LocationLiveData(context: Context) : LiveData<Location>(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private val googleApiClient: GoogleApiClient

    init {
        googleApiClient = GoogleApiClient.Builder(context, this, this)
                .addApi(LocationServices.API)
                .build()
    }

    override fun onActive() {
        // Wait for the GoogleApiClient to be connected
        googleApiClient.connect()
    }

    override fun onInactive() {
        if (googleApiClient.isConnected) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this)
        }
        googleApiClient.disconnect()
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(connectionHint: Bundle?) {
        // Try to immediately find a location
        val lastLocation = LocationServices.FusedLocationApi
                .getLastLocation(googleApiClient)
        if (lastLocation != null) {
            value = lastLocation
        }
        // Request updates if there’s someone observing
        if (hasActiveObservers()) {
            val FASTEST_INTERVAL: Long = 10
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient,
                    LocationRequest()
                            .setInterval(FASTEST_INTERVAL)
                            .setFastestInterval(FASTEST_INTERVAL),
                    this)
        }
    }

    override fun onLocationChanged(location: Location) {
        // Deliver the location changes
        value = location
    }


    override fun onConnectionSuspended(cause: Int) {
        // Cry softly, hope it comes back on its own
    }

    override fun onConnectionFailed(
            connectionResult: ConnectionResult) {
        // Consider exposing this state as described here:
        // https://d.android.com/topic/libraries/architecture/guide.html#addendum
    }

}
