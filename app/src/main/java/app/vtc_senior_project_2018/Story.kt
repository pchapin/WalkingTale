package app.vtc_senior_project_2018

import app.vtc_senior_project_2018.Waypoint

/**
 * 9/27/2017.
 */
data class Story(val id: Int,
                 val name: String,
                 val waypoints: ArrayList<Waypoint>,
                 val tags: ArrayList<String>,
                 val genre: String,
                 val description: String)