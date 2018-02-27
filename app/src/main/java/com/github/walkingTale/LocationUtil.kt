package com.github.walkingTale

import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun LatLngToLocation(latLng: LatLng): Location {
    val location = Location("")
    location.latitude = latLng.latitude
    location.longitude = latLng.longitude
    return location
}

fun LocationToLatLng(location: Location): LatLng {
    return LatLng(location.latitude, location.longitude)
}
