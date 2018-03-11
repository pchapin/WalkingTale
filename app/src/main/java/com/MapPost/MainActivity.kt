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

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.MapPost.ui.audiorecord.AudioRecordActivity
import com.MapPost.ui.common.LocationLiveData
import com.MapPost.ui.common.dispatchTakePictureIntent
import com.MapPost.vo.Post
import com.MapPost.vo.PostType
import com.MapPost.vo.Status
import com.MapPost.vo.User
import com.amazonaws.mobile.auth.core.IdentityManager
import com.auth0.android.jwt.JWT
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ui.IconGenerator
import com.s3HostName
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_chapter_list.*
import java.io.File
import java.util.*
import java.util.concurrent.ThreadLocalRandom


class MainActivity :
        AppCompatActivity(),
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private val tag = this.javaClass.simpleName
    private lateinit var mMap: GoogleMap
    private var playServicesErrorDialog: Dialog? = null
    private lateinit var mainViewModel: MainViewModel
    private lateinit var location: LatLng
    private var file: File? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var editText: TextInputEditText
    private val markers = mutableListOf<Marker>()
    private val markerWidth = 100
    private val markerHeight = 100
    private var cameraOnUserOnce = false
    private val rcAudio = 123
    private val rcPicture = 1234
    private val rcVideo = 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(this.layoutInflater.inflate(R.layout.bottom_sheet_chapter_list, bottom_sheet))
        editText = bottomSheetDialog.findViewById(R.id.post_edit_text)!!
        Analytics.init(this)
        if (PermissionManager.checkLocationPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, rcLocation, "Location", "Give permission to access location?")) {
            initLocation()
        }
        if (PermissionManager.checkLocationPermission(this, Manifest.permission.RECORD_AUDIO, rcAudio, "Audio", "Give permission to record audio?")) {
            initAudio()
        }
    }

    @SuppressLint("MissingPermission")
    fun initLocation() {
        val lld = LocationLiveData(this)
        lld.observe(this, Observer {
            if (it != null) {
                location = locationToLatLng(it)
                mMap.isMyLocationEnabled = true
                locationListener()
                userSetup()
                cameraButton()
                textButton()
                myLocationButton()
                nearbyPosts()
                lld.removeObservers(this)
            }
        })
    }

    fun initAudio() {
        videoButton()
        audioButton()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            rcLocation -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initLocation()
                } else {
                    Toast.makeText(this, "Please enable location permissions for this app.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            rcAudio -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAudio()
                } else {
                    Toast.makeText(this, "Please enable audio permissions for this app.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun nearbyPosts() {
        mainViewModel.getNearbyPosts().observe(this, Observer {

            if (it?.data == null) return@Observer

            markers.map { it.remove() }

            for (post in it.data) {

                val iconGenerator = IconGenerator(this)
                val imageView = ImageView(this)
                imageView.layoutParams = ViewGroup.LayoutParams(markerWidth, markerHeight)
                var markerOptions: MarkerOptions
                val random = ThreadLocalRandom.current().nextDouble(-.00000000000001, 1.00000000000000000001)
                val location = LatLng(post.latitude * random, post.longitude * random)

                when (post.type) {
                    PostType.TEXT -> {
                        imageView.setImageResource(R.drawable.ic_textsms_black_24dp)
                        iconGenerator.setContentView(imageView)
                        markerOptions = MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                                .position(location)
                        markers.add(mMap.addMarker(markerOptions))
                    }
                    PostType.AUDIO -> {
                        imageView.setImageResource(R.drawable.ic_audiotrack_black_24dp)
                        iconGenerator.setContentView(imageView)
                        markerOptions = MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                                .position(location)
                        markers.add(mMap.addMarker(markerOptions))
                    }
                    PostType.PICTURE ->
                        Glide.with(this)
                                .asBitmap()
                                .load(s3HostName + post.content)
                                .apply(RequestOptions().centerCrop())
                                .into(object : SimpleTarget<Bitmap>(markerWidth, markerHeight) {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>) {
                                        imageView.setImageBitmap(resource)
                                        iconGenerator.setContentView(imageView)
                                        markerOptions = MarkerOptions()
                                                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                                                .position(location)
                                        markers.add(mMap.addMarker(markerOptions))
                                    }
                                })
                }
            }
        })
    }

    private fun textButton() {
        text_button.setOnClickListener({
            //            bottomSheetDialog.show()
//            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.showSoftInput(editText, SHOW_IMPLICIT)
            val post = Post(
                    cognitoId,
                    UUID.randomUUID().toString(),
                    Date().time.toString(),
                    location.latitude,
                    location.longitude,
                    mutableListOf(),
                    PostType.TEXT,
                    "nice"
            )
            mainViewModel.putPost(post).observe(this, Observer {
                if (it != null && it.status == Status.SUCCESS) {
                    val user = mainViewModel.currentUser!!
                    if (!user.createdPosts.contains(post.postId)) {
                        user.createdPosts.add(post.postId)
                    }
                    mainViewModel.currentUser = user
                    mainViewModel.putUser(user).observe(this, Observer {
                        if (it != null) Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show()
                    })
                }
            })
        })
    }

    private fun audioButton() {
        audio_button.setOnClickListener({
            val intent = Intent(this, AudioRecordActivity::class.java)
            intent.type = "audio/mpeg4-generic"
            startActivityForResult(intent, rcAudio)
        })
    }

    private fun cameraButton() {
        camera_button.setOnClickListener({
            file = dispatchTakePictureIntent(rcPicture, this, file)
        })
    }

    private fun videoButton() {
        video_button.setOnClickListener({
            val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            if (takeVideoIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takeVideoIntent, rcVideo)
            }
        })
    }

    private fun myLocationButton() {
        my_location_button.setOnClickListener({
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().zoom(DEFAULT_ZOOM).target(location).build()))
        })
    }

    private fun locationListener() {
        LocationLiveData(this).observe(this, Observer {
            if (it != null) {
                if (!cameraOnUserOnce) {
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().zoom(DEFAULT_ZOOM).target(location).build()))
                    cameraOnUserOnce = true
                }
                location = locationToLatLng(it)
            }
        })
    }

    /**
     * Get user if they exist in the dynamo db
     * Put user if they do not exist
     */
    private fun userSetup() {
        mainViewModel.getUser(cognitoId).observe(this, Observer { userResource ->
            if (userResource != null) {
                when (userResource.status) {
                    Status.ERROR -> createNewUser()
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        mainViewModel.currentUser = userResource.data
                        Analytics.logEvent(Analytics.EventType.UserLogin, tag)
                    }
                }
            }
        })
    }

    private fun createNewUser() {
        val user = User(cognitoId, cognitoUsername, mutableListOf(), "none", mutableListOf())
        mainViewModel.currentUser = user
        mainViewModel.putUser(user).observe(this, Observer {
            if (it != null) {
                when (it.status) {
                    Status.SUCCESS -> {
                        Analytics.logEvent(Analytics.EventType.CreatedUser, tag)
                    }
                    Status.ERROR -> {
                    }
                    Status.LOADING -> {
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

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        mMap.setOnMarkerClickListener(this)

        // Change tilt
        val cameraPosition = CameraPosition.Builder()
                .target(mMap.cameraPosition.target)
                .tilt(60f).build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        val mUiSettings = mMap.uiSettings
        mUiSettings.isMapToolbarEnabled = false
        mUiSettings.isZoomControlsEnabled = true
        mUiSettings.isScrollGesturesEnabled = true
        mUiSettings.isZoomGesturesEnabled = true
        mUiSettings.isTiltGesturesEnabled = false
        mUiSettings.isRotateGesturesEnabled = false
        mUiSettings.isCompassEnabled = false
        mUiSettings.isMyLocationButtonEnabled = false
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == rcPicture && resultCode == RESULT_OK) {
            val post = Post()
            post.postId = UUID.randomUUID().toString()
            post.userId = MainActivity.cognitoId
            post.content = file!!.absolutePath
            post.type = PostType.PICTURE
            post.latitude = location.latitude
            post.longitude = location.longitude
            post.dateTime = Date().time.toString()
            // Put the file in S3
            mainViewModel.putFile(Pair(post, this)).observe(this, Observer {
                if (it != null && it.status == Status.SUCCESS) {
                    val newPost = it.data!!
                    // Add the post to DDB
                    mainViewModel.putPost(newPost).observe(this, Observer {
                        if (it != null && it.status == Status.SUCCESS) {
                            val user = mainViewModel.currentUser!!
                            if (!user.createdPosts.contains(newPost.content)) {
                                user.createdPosts.add(newPost.content)
                            }
                            // Update the users set of created posts
                            mainViewModel.putUser(user).observe(this, Observer {
                                Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show()
                            })
                        }
                    })
                }
            })
        } else if (requestCode == rcAudio && resultCode == RESULT_OK) {
            val post = Post(
                    cognitoId,
                    getRandomPostId(),
                    getDate(),
                    location.latitude,
                    location.longitude,
                    mutableListOf<String>(),
                    PostType.AUDIO,
                    data!!.data.path
            )
            mainViewModel.putFile(Pair(post, this)).observe(this, Observer {
                if (it != null && it.status == Status.SUCCESS) {
                    val newPost = it.data!!
                    // Add the post to DDB
                    mainViewModel.putPost(newPost).observe(this, Observer {
                        if (it != null && it.status == Status.SUCCESS) {
                            val user = mainViewModel.currentUser!!
                            if (!user.createdPosts.contains(newPost.content)) {
                                user.createdPosts.add(newPost.content)
                            }
                            // Update the users set of created posts
                            mainViewModel.putUser(user).observe(this, Observer {
                                Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show()
                            })
                        }
                    })
                }
            })
        } else if (requestCode == rcVideo && resultCode == RESULT_OK) {
            val videoUri = intent.data
            video_view.setVideoURI(videoUri)
            video_view.setOnPreparedListener({
                it.isLooping = true
                video_view.start()
            })
        }
    }

    companion object {

        fun getDate(): String {
            return Date().time.toString()
        }

        fun getRandomPostId(): String {
            return UUID.randomUUID().toString()
        }

        fun locationToLatLng(location: Location): LatLng {
            return LatLng(location.latitude, location.longitude)
        }

        fun latLngToLocation(latLng: LatLng): Location {
            val location = Location("")
            location.latitude = latLng.latitude
            location.longitude = latLng.longitude
            return location
        }

        const val DEFAULT_ZOOM = 18f
        val rcLocation = 1
        val cognitoId: String
            get() = IdentityManager.getDefaultIdentityManager().cachedUserID

        val cognitoUsername: String
            get() {
                val cognitoToken = IdentityManager.getDefaultIdentityManager().currentIdentityProvider.token
                val jwt = JWT(cognitoToken)
                val username = jwt.getClaim("cognito:username").asString()
                return username ?: jwt.getClaim("given_name").asString()!!
            }
    }
}