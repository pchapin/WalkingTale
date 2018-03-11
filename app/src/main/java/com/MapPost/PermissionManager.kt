package com.MapPost

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.MapPost.MainActivity.Companion.rcLocation

object PermissionManager {

    /**
     * Returns true if location permission was given, otherwise returns false
     * */
    fun checkLocationPermission(activity: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(activity)
                        .setTitle("Location Permissions")
                        .setMessage("This app needs location permissions to run.")
                        .setPositiveButton("ok") { _, _ ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(activity,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    rcLocation)
                        }
                        .create()
                        .show()

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        rcLocation)
            }
            return false
        } else {
            return true
        }
    }
}
