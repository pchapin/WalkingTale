/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.MapPost

import android.annotation.SuppressLint
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import com.MapPost.db.WalkingTaleDb
import com.MapPost.vo.Status
import com.MapPost.vo.User
import com.amazonaws.mobile.auth.core.IdentityManager
import com.auth0.android.jwt.JWT
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import java.util.*


//todo
//
//reuse code as much as possible
//so turn play fragment into the main map fragment since it already
//knows how to show images on the map
//
//also will need to create a separate aws mobile hub project
//bc posts will need a location field

class MainActivity :
        AppCompatActivity(),
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    override fun onMarkerClick(p0: Marker?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        mMap!!.isMyLocationEnabled = true
        mMap!!.setOnMarkerClickListener(this)

        // Change tilt
        val cameraPosition = CameraPosition.Builder()
                .target(mMap!!.cameraPosition.target)
                .tilt(60f).build()
        mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        val mUiSettings = mMap!!.uiSettings
        mUiSettings.isMapToolbarEnabled = false
        mUiSettings.isZoomControlsEnabled = false
        mUiSettings.isScrollGesturesEnabled = true
        mUiSettings.isZoomGesturesEnabled = true
        mUiSettings.isTiltGesturesEnabled = false
        mUiSettings.isRotateGesturesEnabled = false
        mUiSettings.isCompassEnabled = false
    }

    private val TAG = this.javaClass.simpleName
    private var mMap: GoogleMap? = null
    internal var viewModelFactory: ViewModelProvider.Factory? = null
    internal var playServicesErrorDialog: Dialog? = null
    private var mainViewModel: MainViewModel? = null
    lateinit var roomDatabase: RoomDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainViewModel = ViewModelProviders.of(this, viewModelFactory!!).get(MainViewModel::class.java)
        roomDatabase = Room.databaseBuilder(this, WalkingTaleDb::class.java, "MapPost.db").fallbackToDestructiveMigration().build()
        // Obtain the FirebaseAnalytics instance.
        Analytics.init(this)
        userSetup(savedInstanceState)
    }

    /**
     * Get user if they exist in the dynamo db
     * Put user if they do not exist
     */
    private fun userSetup(savedInstanceState: Bundle?) {

        mainViewModel!!.getUser(cognitoId).observe(this, Observer { userResource ->
            if (userResource != null) {
                when (userResource.status) {
                    Status.ERROR -> createNewUser()
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        Analytics.logEvent(Analytics.EventType.UserLogin, TAG)
                        if (savedInstanceState == null) {

                        }
                    }
                }
            }
        })
    }

    private fun createNewUser() {
        val user = User()
        user.userId = cognitoId
        user.userName = cognitoUsername
        user.createdPosts = ArrayList()
        user.viewedPosts = ArrayList()
        user.userImage = "none"
        mainViewModel!!.createUser(user).observe(this, Observer {
            if (it != null) {
                when (it.status) {
                    Status.SUCCESS -> {
                        Analytics.logEvent(Analytics.EventType.CreatedUser, TAG)
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Prevents user from using the app unless they have google play services installed.
     * Not having it will prevent the google map from working.
     */
    private fun checkPlayServices() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                if (playServicesErrorDialog == null) {
                    playServicesErrorDialog = googleApiAvailability.getErrorDialog(this, resultCode, 2404)
                    playServicesErrorDialog!!.setCancelable(false)
                }

                if (!playServicesErrorDialog!!.isShowing)
                    playServicesErrorDialog!!.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPlayServices()
    }

    enum class DEBUG_STATE {
        OFF, CREATE, PLAY, PROFILE
    }

    private fun initCurrentChapterObserver() {
//        playViewModel!!.posts.observe(this, Observer { listResource ->
//            if (listResource == null || listResource!!.data == null) return@Observer
//
//            for (post in listResource!!.data!!) {
//
//                val iconGenerator = IconGenerator(this)
//                val imageView = ImageView(this)
//                imageView.layoutParams = ViewGroup.LayoutParams(PlayFragment.MARKER_WIDTH, PlayFragment.MARKER_HEIGHT)
//                val markerOptions: MarkerOptions
//                val location = LatLng(post.latitude!!, post.longitude!!)
//
//                when (post.type) {
//                    PostType.TEXT -> {
//                        imageView.setImageResource(R.drawable.ic_textsms_black_24dp)
//                        iconGenerator.setContentView(imageView)
//                        markerOptions = MarkerOptions()
//                                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
//                                .position(location)
//                        markers.add(mMap!!.addMarker(markerOptions))
//                    }
//                    PostType.AUDIO -> {
//                        imageView.setImageResource(R.drawable.ic_audiotrack_black_24dp)
//                        iconGenerator.setContentView(imageView)
//                        markerOptions = MarkerOptions()
//                                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
//                                .position(location)
//                        markers.add(mMap!!.addMarker(markerOptions))
//                    }
////                    PostType.PICTURE -> Glide.with(this!!)
////                            .asBitmap()
////                            .load(s3HostName + post.content!!)
////                            .apply(RequestOptions().centerCrop())
////                            .into<Bitmap>(object : SimpleTarget<Bitmap>(MARKER_WIDTH, MARKER_HEIGHT) {
////                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>) {
////                                    imageView.setImageBitmap(resource)
////                                    iconGenerator.setContentView(imageView)
////                                    val markerOptions = MarkerOptions()
////                                            .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
////                                            .position(location)
////                                    markers.add(mMap!!.addMarker(markerOptions))
////                                }
////                            })
//                }
//            }
//
//        })
    }

    companion object {
        val DEBUG_MODE = DEBUG_STATE.OFF
        val SP_USER_ID_KEY = "SP_USER_ID_KEY"
        val SP_USERNAME_KEY = "SP_USERNAME_KEY"

        val cognitoId: String
            get() = IdentityManager.getDefaultIdentityManager().cachedUserID

        val cognitoUsername: String?
            get() {
                val cognitoToken = IdentityManager.getDefaultIdentityManager().currentIdentityProvider.token
                val jwt = JWT(cognitoToken)
                val username = jwt.getClaim("cognito:username").asString()
                return username ?: jwt.getClaim("given_name").asString()
            }
    }
}