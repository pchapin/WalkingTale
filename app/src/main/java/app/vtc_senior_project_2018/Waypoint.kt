package app.vtc_senior_project_2018

import android.location.Location

/**
 * 9/27/2017.
 */
data class Waypoint(val id: Int,
                    val location: Location,
                    val expositions: ArrayList<Exposition>)