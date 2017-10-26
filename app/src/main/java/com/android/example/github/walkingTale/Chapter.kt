package com.android.example.github.walkingTale

import android.location.Location

data class Chapter(var expositions: ArrayList<Exposition> = ArrayList(),
                   var name: String,
                   var location: Location,
                   var id: Int,
                   var radius: Int)