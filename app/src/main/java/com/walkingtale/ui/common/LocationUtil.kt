package com.walkingtale.ui.common

import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun latLngToLocation(latLng: LatLng): Location {
    val location = Location("")
    location.latitude = latLng.latitude
    location.longitude = latLng.longitude
    return location
}

fun locationToLatLng(location: Location): LatLng {
    return LatLng(location.latitude, location.longitude)
}
