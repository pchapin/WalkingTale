package com.talkingwhale.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.location.Location
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
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
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.talkingwhale.R
import com.talkingwhale.activities.OverflowActivity.Companion.POST_LIST_KEY
import com.talkingwhale.activities.PostViewActivity.Companion.POST_KEY
import com.talkingwhale.activities.TextInputActivity.Companion.LATITUDE_KEY
import com.talkingwhale.activities.TextInputActivity.Companion.LONGITUDE_KEY
import com.talkingwhale.databinding.ActivityMainBinding
import com.talkingwhale.db.AppDatabase
import com.talkingwhale.pojos.*
import com.talkingwhale.pojos.PostType.*
import com.talkingwhale.repository.PostRepository
import com.talkingwhale.ui.MultiDrawable
import com.talkingwhale.util.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import kotlin.concurrent.thread


class MainActivity :
        AppCompatActivity(),
        OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<Post>,
        ClusterManager.OnClusterInfoWindowClickListener<Post>,
        ClusterManager.OnClusterItemClickListener<Post>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Post> {

    private val tag = this.javaClass.simpleName
    private lateinit var mMap: GoogleMap
    private var playServicesErrorDialog: Dialog? = null
    private lateinit var mainViewModel: MainViewModel
    /**The users current location*/
    private lateinit var location: LatLng
    private var file: File? = null
    private val markerWidth = 200
    private val markerHeight = 200
    private var cameraOnUserOnce = false
    private val rcAudio = 1
    private val rcPicture = 2
    private val rcVideo = 3
    private val rcText = 4
    private val rcLocation = 5
    private val minPostDistanceMeters = 30
    private lateinit var binding: ActivityMainBinding
    private var isLinking = false
    private lateinit var mClusterManager: ClusterManager<Post>
    private var lastClusterCenter = LatLng(0.0, 0.0)
    /** Used to get new posts if camera moves too far */
    private var lastCameraCenter: LatLng? = null
    /** Needed to filter ddb results */
    private lateinit var lastCornerLatLng: PostRepository.CornerLatLng
    private var selectedFilterItems = mutableListOf<Int>()
    private lateinit var selectedFilterItemsBoolean: BooleanArray
    private lateinit var iconGenerator: IconGenerator
    private lateinit var iconThread: Thread
    private var postList: List<Post>? = null
    private lateinit var currentUser: User
    private lateinit var db: AppDatabase
    private var polyLinePoints = mutableListOf<LatLng>()
    private var polygon: Polygon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        selectedFilterItemsBoolean = BooleanArray(resources.getStringArray(R.array.genre_array).size)
        iconGenerator = IconGenerator(this)
        Analytics.init(this)
        navigationDrawer()
        db = AppDatabase.getAppDatabase(this)
        if (PermissionManager.checkLocationPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, rcLocation, "Location", "Give permission to access location?")) {
            initLocation()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {
                    // Open the navigation drawer when the home icon is selected from the toolbar.
                    drawer_layout.openDrawer(GravityCompat.START)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    private fun navigationDrawer() {
        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_help -> {
                    startActivity(Intent(this, HelpActivity::class.java))
                }
                R.id.action_about -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://toddcooke.github.io/walking-tale-site/")))
                }
                R.id.action_settings -> {
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

    private fun iconThread() {
        // Update marker icons after they have been placed
        iconThread = thread(false) {
            while (true) {
                runOnUiThread {
                    for (post in postList!!) {
                        if (post.type in listOf(AUDIO, TEXT)) continue
                        var marker: Marker?
                        try {
                            marker = mClusterManager.markerCollection.markers.toList().first { it.title == post.postId }
                        } catch (e: NoSuchElementException) {
                            continue
                        }
                        Glide.with(applicationContext)
                                .asBitmap()
                                .load(resources.getString(R.string.s3_hostname) + post.content)
                                .apply(RequestOptions().centerCrop())
                                .into(object : SimpleTarget<Bitmap>(markerWidth, markerHeight) {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>) {
                                        val imageView = ImageView(this@MainActivity)
                                        imageView.setImageBitmap(resource)
                                        iconGenerator.setContentView(imageView)
                                        marker.setIcon(MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())).icon)
                                    }
                                })
                    }
                }
                Thread.sleep(10000)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocation() {
        val lld = LocationLiveData(this)
        lld.observe(this, Observer {
            if (it != null) {
                lld.removeObservers(this)
                location = locationToLatLng(it)
                locationListener()
                userObserver()
                cameraButton()
                textButton()
                myLocationButton()
                nearbyPosts()
                videoButton()
                audioButton()
                linkButton()
                filterButton()
                iconThread()
                dragListener()

                class CustomClusterManager : ClusterManager<Post>(this, mMap) {
                    override fun onCameraIdle() {
                        super.onCameraIdle()
                        possiblyGetNewPosts()
                    }
                }
                mClusterManager = CustomClusterManager()
                mClusterManager.renderer = PostRenderer()
                mMap.setOnCameraIdleListener(mClusterManager)
                mMap.setOnMarkerClickListener(mClusterManager)
                mMap.setOnInfoWindowClickListener(mClusterManager)
                mClusterManager.setOnClusterClickListener(this)
                mClusterManager.setOnClusterInfoWindowClickListener(this)
                mClusterManager.setOnClusterItemClickListener(this)
                mClusterManager.setOnClusterItemInfoWindowClickListener(this)
            }
        })
    }

    private fun possiblyGetNewPosts() {
        if (lastCameraCenter == null) return
        // If camera has moved too far, get new posts
        if (SphericalUtil.computeDistanceBetween(lastCameraCenter, mMap.projection.visibleRegion.latLngBounds.center) > 250) {
            lastCameraCenter = mMap.projection.visibleRegion.latLngBounds.center
            mainViewModel.setPostBounds(lastCornerLatLng)
        }
    }

    override fun onClusterClick(cluster: Cluster<Post>?): Boolean {

        // Create the builder to collect all essential cluster items for the bounds.
        val builder = LatLngBounds.builder()
        for (item in cluster!!.items) {
            builder.include(item.position)
        }
        // Get the LatLngBounds
        val bounds = builder.build()

        // Show overflow if the bounds has not changed and
        // the markers are still clustered
        if (bounds.center == lastClusterCenter) {

            if (!insideRadius(bounds.center)) return true

            db.postDao().insertPosts(cluster.items.toList())
            val intent = Intent(this, OverflowActivity::class.java)
            intent.putExtra(POST_LIST_KEY, cluster.items.map { it.postId }.toTypedArray())
            startActivity(intent)
        }

        // Animate camera to the bounds
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        lastClusterCenter = bounds.center
        return true
    }

    override fun onClusterInfoWindowClick(p0: Cluster<Post>?) {
    }

    private fun insideRadius(latLng: LatLng): Boolean {
        val distanceFromPost = SphericalUtil.computeDistanceBetween(location, latLng)
        if (distanceFromPost > minPostDistanceMeters) {
            Toast.makeText(this, "You must be ${(distanceFromPost - minPostDistanceMeters).toInt()} meters closer to this post to access it.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onClusterItemClick(marker: Post?): Boolean {
        val post = marker!!
        if (!insideRadius(post.position)) return true
        binding.post = post
        db.postDao().insert(post)
        val intent = Intent(this, PostViewActivity::class.java)
        intent.putExtra(POST_KEY, post.postId)
        startActivity(intent)
        return true
    }

    override fun onClusterItemInfoWindowClick(p0: Post?) {
    }

    private inner class PostRenderer : DefaultClusterRenderer<Post>(applicationContext, mMap, mClusterManager) {
        private val iconGenerator = IconGenerator(applicationContext)
        private val mClusterIconGenerator = IconGenerator(applicationContext)
        private val imageView: ImageView
        private val mClusterImageView: ImageView
        private val mDimension: Int

        init {
            @SuppressLint("InflateParams")
            val multiProfile = layoutInflater.inflate(R.layout.multi_profile, null)
            mClusterIconGenerator.setContentView(multiProfile)
            mClusterImageView = multiProfile.findViewById(R.id.image)
            imageView = ImageView(applicationContext)
            mDimension = resources.getDimension(R.dimen.custom_profile_image).toInt()
            imageView.layoutParams = ViewGroup.LayoutParams(mDimension, mDimension)
            val padding = resources.getDimension(R.dimen.custom_profile_padding).toInt()
            imageView.setPadding(padding, padding, padding, padding)
            iconGenerator.setContentView(imageView)
        }

        override fun onBeforeClusterItemRendered(post: Post?, markerOptions: MarkerOptions?) {
            // Draw a single Post.
            imageView.setImageResource(getDrawableForPost(post!!))
            iconGenerator.setContentView(imageView)
            markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
            markerOptions.title(post.postId)
        }

        override fun onBeforeClusterRendered(cluster: Cluster<Post>, markerOptions: MarkerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            val profilePhotos = ArrayList<Drawable>(Math.min(4, cluster.size))
            val width = mDimension
            val height = mDimension

            for (p in cluster.items) {
                // Draw 4 at most.
                if (profilePhotos.size == 4) break
                val drawable = resources.getDrawable(getDrawableForPost(p), theme)
                drawable.setBounds(0, 0, width, height)
                profilePhotos.add(drawable)
            }
            val multiDrawable = MultiDrawable(profilePhotos)
            multiDrawable.setBounds(0, 0, width, height)

            mClusterImageView.setImageDrawable(multiDrawable)
            val icon = mClusterIconGenerator.makeIcon(cluster.size.toString())
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
        }

        override fun shouldRenderAsCluster(cluster: Cluster<Post>?): Boolean {
            // Always render clusters.
            return cluster!!.size > 1
        }
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
                    audio_button.performClick()
                }
            }
            rcVideo -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    video_button.performClick()
                }
            }
        }
    }

    private fun showAllPosts() {
        mClusterManager.clearItems()
        mClusterManager.addItems(postList)
        mClusterManager.cluster()
    }

    private fun showOnlyUsersPosts() {
        mClusterManager.clearItems()
        val usersPosts = postList!!.filter { it.userId == cognitoId }
        mClusterManager.addItems(usersPosts)
        mClusterManager.cluster()
    }

    private fun nearbyPosts() {
        mainViewModel.localPosts.observe(this, Observer {

            if (it?.data == null) return@Observer

            postList = it.data
            showAllPosts()
            if (iconThread.state == Thread.State.NEW) {
                iconThread.start()
            }
        })
    }

    private fun textButton() {
        text_button.setOnClickListener({
            val intent = Intent(this, TextInputActivity::class.java)
            intent.putExtra(LATITUDE_KEY, location.latitude)
            intent.putExtra(LONGITUDE_KEY, location.longitude)
            startActivityForResult(intent, rcText)
        })
    }

    private fun audioButton() {
        audio_button.setOnClickListener({
            if (PermissionManager.checkLocationPermission(this, Manifest.permission.RECORD_AUDIO, rcAudio, "Audio", "Give permission to record audio?")) {
                val intent = Intent(this, AudioRecordActivity::class.java)
                intent.type = "audio/mpeg4-generic"
                startActivityForResult(intent, rcAudio)
            }
        })
    }

    private fun cameraButton() {
        camera_button.setOnClickListener({
            file = dispatchTakePictureIntent(rcPicture, this, file)
        })
    }

    private fun videoButton() {
        video_button.setOnClickListener({
            if (PermissionManager.checkLocationPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, rcVideo, "Storage", "Give permission to access storage?")) {
                val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                if (takeVideoIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(takeVideoIntent, rcVideo)
                }
            }
        })
    }

    private fun myLocationButton() {
        my_location_button.setOnClickListener({
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().zoom(DEFAULT_ZOOM).target(location).build()))
        })
    }

    private fun dragListener() {
        // from https://stackoverflow.com/a/20916931/3569329
        drag_map.setOnTouchListener({ _: View?, event: MotionEvent? ->

            if (!isLinking) return@setOnTouchListener false

            val x = Math.round(event!!.x)
            val y = Math.round(event.y)

            val latLng = mMap.projection.fromScreenLocation(Point(x, y))

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    polyLinePoints.add(latLng)
                    drawMap()
                }
                MotionEvent.ACTION_MOVE -> {
                    polyLinePoints.add(latLng)
                    drawMap()
                }
            }
            return@setOnTouchListener true
        })
    }

    private fun drawMap() {
        if (polygon == null) {
            polygon = mMap.addPolygon(
                    PolygonOptions()
                            .addAll(polyLinePoints)
                            .strokeColor(Color.BLUE)
                            .fillColor(Color.BLUE)
                            .strokeWidth(7f)
            )
        }
        polygon?.points = polygon?.points.also {
            it?.addAll(polyLinePoints)
        }
    }

    private fun linkButton() {
        link_button.setOnClickListener({
            if (!isLinking) {
                isLinking = !isLinking
                link_button.size = FloatingActionButton.SIZE_MINI
                showOnlyUsersPosts()
            } else {
                isLinking = !isLinking
                link_button.size = FloatingActionButton.SIZE_NORMAL

                if (polygon != null) {
                    // For each visible post
                    for (post in mClusterManager.algorithm.items) {
                        // if polygon contains post
                        if (PolyUtil.containsLocation(LatLng(post.latitude, post.longitude), polygon!!.points, false)) {
                            // todo: add to collection
                            Log.i(tag, post.content)
                        }
                    }
                    polygon?.remove()
                    polygon = null
                    polyLinePoints.clear()
                }

                showAllPosts()
            }
        })
    }

    private fun filterButton() {
        filter_button.setOnClickListener({
            AlertDialog.Builder(this)
                    .setTitle("Filter posts")
                    .setMultiChoiceItems(
                            R.array.genre_array,
                            selectedFilterItemsBoolean,
                            { _, which, isChecked ->
                                if (isChecked) {
                                    selectedFilterItems.add(which)
                                } else if (selectedFilterItems.contains(which)) {
                                    selectedFilterItems.remove(which)
                                }
                            }).setPositiveButton("ok") { _, _ ->
                        run {
                        }
                    }
                    .show()
        })
    }

    private fun locationListener() {
        LocationLiveData(this).observe(this, Observer {
            if (it != null) {
                if (!cameraOnUserOnce) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().zoom(DEFAULT_ZOOM).target(location).build()))
                    cameraOnUserOnce = true
                    lastCornerLatLng = PostRepository.CornerLatLng(
                            mMap.projection.visibleRegion.latLngBounds.northeast,
                            mMap.projection.visibleRegion.latLngBounds.southwest
                    )
                    lastCameraCenter = mMap.projection.visibleRegion.latLngBounds.center
                    mainViewModel.setPostBounds(lastCornerLatLng)
                }
                location = locationToLatLng(it)
                lastCornerLatLng = PostRepository.CornerLatLng(
                        mMap.projection.visibleRegion.latLngBounds.northeast,
                        mMap.projection.visibleRegion.latLngBounds.southwest
                )
            }
        })
    }

    /**
     * Get user if they exist in the dynamo db
     * Put user if they do not exist
     */
    private fun userObserver() {
        mainViewModel.currentUser.observe(this, Observer {
            if (it != null) {
                when (it.status) {
                    Status.ERROR -> createNewUser()
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        binding.user = it.data
                        currentUser = it.data!!
                        Analytics.logEvent(Analytics.EventType.UserLogin, tag)
                    }
                }
            }
        })
        mainViewModel.setUserId(cognitoId)
    }

    private fun createNewUser() {
        val user = User(cognitoId, cognitoUsername, mutableListOf(), "none", mutableListOf())
        mainViewModel.putUser(user).observe(this, Observer {
            if (it != null) {
                when (it.status) {
                    Status.SUCCESS -> {
                        Analytics.logEvent(Analytics.EventType.CreatedUser, tag)
                        mainViewModel.setUserId(cognitoId)
                    }
                    Status.ERROR -> {
                    }
                    Status.LOADING -> {
                    }
                }
            }
        })
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
        if (IdentityManager.getDefaultIdentityManager() == null) {
            val intent = Intent(this, AuthenticatorActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            checkPlayServices()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        val mUiSettings = mMap.uiSettings
        mUiSettings.isMapToolbarEnabled = false
        mUiSettings.isZoomControlsEnabled = false
        mUiSettings.isScrollGesturesEnabled = true
        mUiSettings.isZoomGesturesEnabled = true
        mUiSettings.isTiltGesturesEnabled = false
        mUiSettings.isRotateGesturesEnabled = false
        mUiSettings.isCompassEnabled = false
        mUiSettings.isMyLocationButtonEnabled = false
        mUiSettings.isIndoorLevelPickerEnabled = false
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return

        var postType: PostType? = null
        var content: String? = null
        when (requestCode) {
            rcPicture -> {
                content = file!!.absolutePath
                postType = PICTURE
            }
            rcAudio -> {
                content = data!!.data.path
                postType = AUDIO
            }
            rcVideo -> {
                val videoUri = data!!.data
                val videoFile = File(UriUtil.getPath(this, videoUri))
                content = videoFile.absolutePath
                postType = VIDEO

                val retriever = MediaMetadataRetriever()
                // use one of overloaded setDataSource() functions to set your data source
                retriever.setDataSource(this, Uri.fromFile(videoFile))
                val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()
                val timeMilliseconds = time.toLong()
                if (timeMilliseconds > 6000) {
                    Toast.makeText(this, "Videos must be at most 6 seconds.", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            rcText -> {
                content = data!!.getStringExtra(TextInputActivity.TEXT_KEY)
                postType = TEXT
            }
        }
        val post = Post(
                cognitoId,
                getRandomPostId(),
                getDate(),
                location.latitude,
                location.longitude,
                mutableListOf(),
                postType!!,
                content!!,
                mutableListOf()
        )

        // Put the file in S3
        mainViewModel.putFile(Pair(post, this)).observe(this, Observer {
            if (it != null && it.status == Status.SUCCESS) {
                val newPost = it.data!!
                // Add the post to DDB
                mainViewModel.putPost(newPost).observe(this, Observer {
                    if (it != null && it.status == Status.SUCCESS) {
                        if (!currentUser.createdPosts.contains(newPost.content)) {
                            currentUser.createdPosts.add(newPost.content)
                        }
                        // Update the users set of created posts
                        mainViewModel.putUser(currentUser).observe(this, Observer {
                            if (it != null && it.status == Status.SUCCESS) {
                                Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show()
                                mainViewModel.setPostBounds(lastCornerLatLng)
                            }
                        })
                    }
                })
            }
        })
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

        const val DEFAULT_ZOOM = 18f
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