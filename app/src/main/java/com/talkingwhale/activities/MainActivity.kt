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
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.location.Location
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
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
        Fragment(),
        OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<Post>,
        ClusterManager.OnClusterInfoWindowClickListener<Post>,
        ClusterManager.OnClusterItemClickListener<Post>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Post> {

    private lateinit var mMap: GoogleMap
    private var playServicesErrorDialog: Dialog? = null
    private lateinit var mainViewModel: MainViewModel
    /**The users current location*/
    private lateinit var location: LatLng
    private var file: File? = null
    private var cameraOnUserOnce = false
    private val rcAudio = 1
    private val rcPicture = 2
    private val rcVideo = 3
    private val rcText = 4
    private val rcLocation = 5
    private val rcMyPosts = 6
    private val rcSettings = 7
    private lateinit var binding: ActivityMainBinding
    private var isLinking = false
    private lateinit var mClusterManager: ClusterManager<Post>
    private var lastClusterCenter = LatLng(0.0, 0.0)
    /** Used to get new posts if camera moves too far. Is larger than currentCornerLatLng */
    private var cameraBounds: PostRepository.CornerLatLng? = null
    /** Needed to filter ddb results */
    private lateinit var currentCornerLatLng: PostRepository.CornerLatLng
    private lateinit var iconGenerator: IconGenerator
    private lateinit var iconThread: Thread
    private var postList: List<Post>? = null
    private lateinit var currentUser: User
    private lateinit var db: AppDatabase
    private var polyLinePoints = mutableListOf<LatLng>()
    private var polygon: Polygon? = null
    private var outerCircle: Circle? = null
    private var innerCircle: Circle? = null
    private val minPostDistanceMeters = 30
    private val cameraDiff = .1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_main, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        authOrExit()
        mainViewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        iconGenerator = IconGenerator(context)
        Analytics.init(context!!)
        db = AppDatabase.getAppDatabase(context!!)
        if (PermissionManager.checkLocationPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION, rcLocation, "Location", "Give permission to access location?")) {
            initLocation()
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
                        Glide.with(context)
                                .asBitmap()
                                .load(resources.getString(R.string.s3_hostname) + post.content)
                                .apply(RequestOptions().centerCrop())
                                .into(object : SimpleTarget<Bitmap>(200, 200) {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>) {
                                        val imageView = ImageView(context)
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

        authOrExit()

        val lld = LocationLiveData(context!!)
        lld.observe(this, Observer {
            if (it != null) {
                lld.removeObservers(this)
                location = locationToLatLng(it)
                locationListener()
                userObserver()
                cameraButton()
                textButton()
                myLocationButton()
                videoButton()
                audioButton()
                groupButton()
                doneButton()
                iconThread()
                dragListener()

                class CustomClusterManager : ClusterManager<Post>(context, mMap) {
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
                mMap.setMinZoomPreference(15f)
                setMapStyle()
                mClusterManager.setOnClusterClickListener(this)
                mClusterManager.setOnClusterInfoWindowClickListener(this)
                mClusterManager.setOnClusterItemClickListener(this)
                mClusterManager.setOnClusterItemInfoWindowClickListener(this)
                nearbyPosts()
            }
        })
    }

    private fun setMapStyle() {
        val sp = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context)
        val nightModeOff = sp.getBoolean(resources.getString(R.string.pref_key_map_mode), false)
        val mapStyle = if (nightModeOff) R.raw.map_style else R.raw.mapstyle_night
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, mapStyle))
    }

    /**
     * If camera has moved too far, fetch new posts
     * */
    private fun possiblyGetNewPosts() {
        if (cameraBounds == null) return
        if (isLinking) return
        if (done_button.visibility == View.VISIBLE) return

        if (
                currentCornerLatLng.northEast.latitude > cameraBounds!!.northEast.latitude ||
                currentCornerLatLng.northEast.longitude > cameraBounds!!.northEast.longitude ||
                currentCornerLatLng.southWest.latitude < cameraBounds!!.southWest.latitude ||
                currentCornerLatLng.southWest.longitude < cameraBounds!!.southWest.longitude
        ) {
            Log.i(TAG, "Fetching new posts")
            cameraBounds = newExpandedBounds(currentCornerLatLng, cameraDiff)
            mainViewModel.setPostBounds(cameraBounds as PostRepository.CornerLatLng)
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
            val intent = Intent(context, OverflowActivity::class.java)
            intent.putExtra(POST_LIST_KEY, cluster.items.map { it.postId }.toTypedArray())
            startActivityForResult(intent, PostViewActivity.RC_POST_VIEW)
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
            Toast.makeText(context, "You must be ${(distanceFromPost - minPostDistanceMeters).toInt()} meters closer to this post to access it.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onClusterItemClick(marker: Post?): Boolean {
        val post = marker!!
        if (!insideRadius(post.position)) return true
        binding.post = post
        db.postDao().insert(post)
        val intent = Intent(context, PostViewActivity::class.java)
        intent.putExtra(POST_KEY, post.postId)
        intent.putExtra(PostViewActivity.POST_GROUP_KEY, post.groupId)
        startActivityForResult(intent, PostViewActivity.RC_POST_VIEW)
        return true
    }

    override fun onClusterItemInfoWindowClick(p0: Post?) {
    }

    private inner class PostRenderer : DefaultClusterRenderer<Post>(context, mMap, mClusterManager) {
        private val iconGenerator = IconGenerator(context)
        private val mClusterIconGenerator = IconGenerator(context)
        private val imageView: ImageView
        private val mClusterImageView: ImageView
        private val mDimension: Int

        init {
            @SuppressLint("InflateParams")
            val multiProfile = layoutInflater.inflate(R.layout.multi_profile, null)
            mClusterIconGenerator.setContentView(multiProfile)
            mClusterImageView = multiProfile.findViewById(R.id.image)
            imageView = ImageView(context)
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
                val drawable = resources.getDrawable(getDrawableForPost(p), activity?.theme)
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
                    Toast.makeText(context, "Please enable location permissions for this app.", Toast.LENGTH_SHORT).show()
                    activity?.finish()
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

    private fun showRecentPosts() {
        mClusterManager.clearItems()
        val recentItems = postList!!.filter {
            Date().time - it.dateTime.toLong() < 1000 * 60 * 60 * 24
        }
        mClusterManager.addItems(recentItems)
        mClusterManager.cluster()
    }

    private fun showAllPosts() {
        mClusterManager.clearItems()
        mClusterManager.addItems(postList)
        mClusterManager.cluster()
    }

    private fun showOnlyUsersPosts(userId: String) {
        mClusterManager.clearItems()
        val usersPosts = postList!!.filter { it.userId == userId }
        mClusterManager.addItems(usersPosts)
        mClusterManager.cluster()
    }

    private fun showOnlyGroupPosts(groupId: String) {
        mClusterManager.clearItems()
        val usersPosts = postList!!.filter { it.groupId == groupId }
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
            val intent = Intent(context, TextInputActivity::class.java)
            startActivityForResult(intent, rcText)
        })
    }

    private fun audioButton() {
        audio_button.setOnClickListener({
            if (PermissionManager.checkLocationPermission(activity!!, Manifest.permission.RECORD_AUDIO, rcAudio, "Audio", "Give permission to record audio?")) {
                (activity as AppCompatActivity).navigateToFragment(AudioRecordActivity())
            }
        })
    }

    private fun cameraButton() {
        camera_button.setOnClickListener({
            file = dispatchTakePictureIntent(rcPicture, activity!!, file)
        })
    }

    private fun videoButton() {
        video_button.setOnClickListener({
            if (PermissionManager.checkLocationPermission(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE, rcVideo, "Storage", "Give permission to access storage?")) {
                val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 6)
                if (takeVideoIntent.resolveActivity(activity?.packageManager) != null) {
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

            if (event.action == MotionEvent.ACTION_MOVE) {
                polyLinePoints.add(latLng)
                drawMap()
            }
            return@setOnTouchListener true
        })
    }

    private fun drawMap() {
        if (polygon == null) {
            polygon = mMap.addPolygon(
                    PolygonOptions()
                            .addAll(polyLinePoints)
                            .strokeColor(resources.getColor(R.color.secondaryColor, activity?.theme))
                            .strokeWidth(7f)
            )
        }
        polygon?.points = polygon?.points.also {
            it?.clear()
            it?.addAll(polyLinePoints)
        }
    }

    private fun groupButton() {
        group_button.setOnClickListener {
            if (!isLinking) {
                isLinking = !isLinking
                showOnlyUsersPosts(cognitoId)
                fabDisplay(false)

            } else {
                isLinking = !isLinking
                fabDisplay(true)

                if (polygon != null) {

                    val postsInGroup = mClusterManager.algorithm.items
                            .filter {
                                it.userId == cognitoId &&
                                        PolyUtil.containsLocation(LatLng(it.latitude, it.longitude), polygon!!.points, false)
                            }
                    val postGroup = PostGroup(cognitoId, getRandomUUID(), postsInGroup.map { it.postId } as MutableList<String>)

                    postsInGroup.map { it.groupId = postGroup.id }
                    if (postsInGroup.isNotEmpty()) {
                        val liveData = mainViewModel.putPostGroup(postGroup)
                        liveData.observe(this, Observer {
                            if (it != null && it.status == Status.SUCCESS) {
                                mainViewModel.putPosts(postsInGroup).observe(this, Observer {
                                    if (it != null && it.status == Status.SUCCESS) {
                                        currentUser.postGroupIds.add(postGroup.id)
                                        mainViewModel.putUser(currentUser).observe(this, Observer {
                                            if (it != null && it.status == Status.SUCCESS) {
                                                Toast.makeText(context, "Post group created!", Toast.LENGTH_SHORT).show()
                                                liveData.removeObservers(this)
                                            }
                                        })
                                    }
                                })
                            }
                        })
                    } else {
                        toast("No posts in group.")
                    }

                    polygon?.remove()
                    polygon = null
                    polyLinePoints.clear()
                }

                showAllPosts()
            }
        }
    }

    private fun filterButton() {
        val filterItems = arrayOf("My Posts", "All Posts", "Recent Posts")
        AlertDialog.Builder(context)
                .setTitle("Filter posts")
                .setItems(filterItems, { _, which ->
                    when (which) {
                        0 -> {
                            snackbar("Showing only your posts")
                            showOnlyUsersPosts(cognitoId)
                        }
                        1 -> {
                            snackbar("Showing all posts")
                            showAllPosts()
                        }
                        2 -> {
                            snackbar("Showing posts under 1 day old")
                            showRecentPosts()
                        }
                    }
                })
                .show()
    }

    private fun locationListener() {
        LocationLiveData(context!!).observe(this, Observer {
            if (it != null) {
                location = locationToLatLng(it)
                if (!cameraOnUserOnce) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().zoom(DEFAULT_ZOOM).target(location).build()))
                    cameraOnUserOnce = true
                    currentCornerLatLng = PostRepository.CornerLatLng(
                            mMap.projection.visibleRegion.latLngBounds.northeast,
                            mMap.projection.visibleRegion.latLngBounds.southwest
                    )
                    cameraBounds = newExpandedBounds(currentCornerLatLng, cameraDiff)
                    mainViewModel.setPostBounds(cameraBounds as PostRepository.CornerLatLng)
                }
                currentCornerLatLng = PostRepository.CornerLatLng(
                        mMap.projection.visibleRegion.latLngBounds.northeast,
                        mMap.projection.visibleRegion.latLngBounds.southwest
                )
                outerCircle?.remove()
                outerCircle = mMap.addCircle(
                        CircleOptions()
                                .center(location)
                                .radius(minPostDistanceMeters.toDouble())
                                .strokeWidth(5f)
                                .strokeColor(resources.getColor(R.color.secondaryColor, activity?.theme)))

                innerCircle?.remove()
                innerCircle = mMap.addCircle(
                        CircleOptions()
                                .center(location)
                                .radius(.4)
                                .fillColor(resources.getColor(R.color.secondaryColor, activity?.theme))
                                .strokeColor(resources.getColor(R.color.secondaryColor, activity?.theme)))
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
                        Analytics.logEvent(Analytics.EventType.UserLogin, TAG)
                    }
                }
            }
        })
        mainViewModel.setCurrentUserId(cognitoId)
    }

    private fun createNewUser() {
        val user = User(cognitoId, cognitoUsername, mutableListOf(), "none", mutableListOf(), mutableListOf())
        val liveData = mainViewModel.putUser(user)
        liveData.observe(this, Observer {
            if (it != null) {
                when (it.status) {
                    Status.SUCCESS -> {
                        Analytics.logEvent(Analytics.EventType.CreatedUser, TAG)
                        mainViewModel.setCurrentUserId(cognitoId)
                        liveData.removeObservers(this)
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
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                if (playServicesErrorDialog == null) {
                    playServicesErrorDialog = googleApiAvailability.getErrorDialog(activity, resultCode, 2404)
                    playServicesErrorDialog!!.setCancelable(false)
                }

                if (!playServicesErrorDialog!!.isShowing)
                    playServicesErrorDialog!!.show()
            }
        }
    }

    private fun authOrExit() {
        if (IdentityManager.getDefaultIdentityManager()?.cachedUserID == null) {
            popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        authOrExit()
        if (::mMap.isInitialized) setMapStyle()
        checkPlayServices()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
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

    private fun doneButton() {
        done_button.setOnClickListener {
            fabDisplay(true)
            showAllPosts()
            if (isLinking) group_button.performClick()
        }
    }

    private fun fabDisplay(show: Boolean) {
        for (i in 0..top_level_constraint_layout.childCount) {
            try {
                val fab = top_level_constraint_layout.getChildAt(i) as FloatingActionButton
                if (show) {
                    fab.show()
                } else {
                    fab.hide()
                }
            } catch (e: ClassCastException) {
            }
        }
        if (show) {
            done_button.hide()
        } else {
            done_button.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
                val videoFile = File(UriUtil.getPath(context, videoUri))
                content = videoFile.absolutePath
                postType = VIDEO

                val retriever = MediaMetadataRetriever()
                // use one of overloaded setDataSource() functions to set your data source
                retriever.setDataSource(context, Uri.fromFile(videoFile))
                val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()
                val timeMilliseconds = time.toLong()
                if (timeMilliseconds > 7000) {
                    toast("Videos must be at most 6 seconds.")
                    return
                }
            }
            rcText -> {
                content = data!!.getStringExtra(TextInputActivity.TEXT_KEY)
                postType = TEXT
            }
            PostViewActivity.RC_POST_VIEW -> {
                val groupId = data?.getStringExtra(PostViewActivity.POST_GROUP_GROUPID_KEY)
                val userId = data?.getStringExtra(PostViewActivity.POST_USERID_KEY)
                if (groupId != null) {
                    showOnlyGroupPosts(groupId)
                    Snackbar.make(top_level_constraint_layout, "Showing group posts", Snackbar.LENGTH_LONG).show()
                    fabDisplay(false)
                } else if (userId != null) {
                    showOnlyUsersPosts(userId)
                    Snackbar.make(top_level_constraint_layout, "Showing users posts", Snackbar.LENGTH_LONG).show()
                    fabDisplay(false)
                }
                return
            }
            rcMyPosts -> {
                // User may have deleted some posts in MyPosts activity, so fetch again
                mainViewModel.setPostBounds(cameraBounds!!)
                return
            }
            rcSettings -> {
                if (data?.getBooleanExtra(SettingsActivity.DELETED_POST_KEY, false) == true) {
                    toast("Account deleted.")
                    popBackStack()
                }
                return
            }
        }

        val post = Post(
                cognitoId,
                getRandomUUID(),
                getDate(),
                location.latitude,
                location.longitude,
                mutableListOf(),
                postType!!,
                content!!,
                null
        )
        mainViewModel.createPost(post, context!!, this, {
            toast("Post created!")
            mClusterManager.addItem(post)
            mClusterManager.cluster()
        })
    }

    companion object {

        fun getDate(): String {
            return Date().time.toString()
        }

        fun getRandomUUID(): String {
            return UUID.randomUUID().toString()
        }

        fun locationToLatLng(location: Location): LatLng {
            return LatLng(location.latitude, location.longitude)
        }

        private const val DEFAULT_ZOOM = 18f
        val cognitoId: String
            get() = IdentityManager.getDefaultIdentityManager().cachedUserID

        val cognitoUsername: String
            get() {
                val cognitoToken = IdentityManager.getDefaultIdentityManager().currentIdentityProvider.token
                val jwt = JWT(cognitoToken)
                val username = jwt.getClaim("cognito:username").asString()
                return username ?: jwt.getClaim("given_name").asString()!!
            }

        private fun newExpandedBounds(cornerLatLng: PostRepository.CornerLatLng, cameraDiff: Double): PostRepository.CornerLatLng {
            val newNeLat = cornerLatLng.northEast.latitude + cameraDiff
            val newNeLong = cornerLatLng.northEast.longitude + cameraDiff
            val newSwLat = cornerLatLng.southWest.latitude - cameraDiff
            val newSwLong = cornerLatLng.southWest.longitude - cameraDiff
            return PostRepository.CornerLatLng(LatLng(newNeLat, newNeLong), LatLng(newSwLat, newSwLong))
        }

        private val TAG = MainActivity::class.java.simpleName
    }
}