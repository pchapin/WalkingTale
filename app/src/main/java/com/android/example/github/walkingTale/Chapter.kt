package com.android.example.github.walkingTale

import com.google.android.gms.maps.model.LatLng

data class Chapter(var expositions: ArrayList<Exposition> = ArrayList(),
                   var name: String,
                   var location: LatLng,
                   var id: Int,
                   var radius: Int)