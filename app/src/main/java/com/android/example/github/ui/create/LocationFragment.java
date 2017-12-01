///*
// * Copyright (C) 2017 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.android.example.github.ui.create;
//
//import android.Manifest;
//import android.app.Activity;
//import android.arch.lifecycle.LifecycleRegistry;
//import android.arch.lifecycle.LifecycleRegistryOwner;
//import android.arch.lifecycle.ViewModelProvider;
//import android.arch.lifecycle.ViewModelProviders;
//import android.content.Intent;
//import android.content.IntentSender;
//import android.content.pm.PackageManager;
//import android.databinding.DataBindingComponent;
//import android.graphics.Bitmap;
//import android.location.Location;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Looper;
//import android.provider.MediaStore;
//import android.provider.Settings;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.design.widget.Snackbar;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.Fragment;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import com.android.example.github.BuildConfig;
//import com.android.example.github.R;
//import com.android.example.github.binding.FragmentDataBindingComponent;
//import com.android.example.github.di.Injectable;
//import com.android.example.github.ui.common.ChapterAdapter;
//import com.android.example.github.ui.common.NavigationController;
//import com.android.example.github.util.AutoClearedValue;
//import com.android.example.github.walkingTale.ExpositionType;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.ResolvableApiException;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.LocationSettingsRequest;
//import com.google.android.gms.location.LocationSettingsStatusCodes;
//import com.google.android.gms.location.SettingsClient;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.Marker;
//
//import java.text.DateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//
//import javax.inject.Inject;
//
//import static android.app.Activity.RESULT_OK;
//import static android.content.Context.ACTIVITY_SERVICE;
//
//
///**
// * The UI Controller for creating a story.
// */
//public class LocationFragment extends Fragment implements
//        LifecycleRegistryOwner,
//        Injectable,
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener,
//        OnMapReadyCallback {
//
//
//    private static final String TAG = LocationFragment.class.getSimpleName();
//
//
//    /**
//     * Constant used in the location settings dialog.
//     */
//    private static final int REQUEST_CHECK_SETTINGS = 0x1;
//
//    /**
//     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
//     */
//    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
//
//    /**
//     * The fastest rate for active location updates. Exact. Updates will never be more frequent
//     * than this value.
//     */
//    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
//            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
//
//    // Keys for storing activity state in the Bundle.
//    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
//    private final static String KEY_LOCATION = "location";
//    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
//    // Constants
//    private static final String REPO_OWNER_KEY = "repo_owner";
//    private static final String REPO_NAME_KEY = "repo_name";
//    public static String AUDIO_KEY_CHAPTER = "AUDIO_KEY_CHAPTER";
//    public static String AUDIO_KEY_EXPOSITION = "AUDIO_KEY_EXPOSITION";
//    private final int RECORD_AUDIO_REQUEST_CODE = 123;
//    private final int TAKE_PICTURE_REQUEST_CODE = 1234;
//    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
//    @Inject
//    ViewModelProvider.Factory viewModelFactory;
//    @Inject
//    NavigationController navigationController;
//    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
//    AutoClearedValue<ChapterAdapter> adapter;
//    /**
//     * Provides access to the Fused Location Provider API.
//     */
//    private FusedLocationProviderClient mFusedLocationClient;
//    /**
//     * Provides access to the Location Settings API.
//     */
//    private SettingsClient mSettingsClient;
//    /**
//     * Stores parameters for requests to the FusedLocationProviderApi.
//     */
//    private LocationRequest mLocationRequest;
//    /**
//     * Stores the types of location services the client is interested in using. Used for checking
//     * settings to determine if the device has optimal location settings.
//     */
//    private LocationSettingsRequest mLocationSettingsRequest;
//    /**
//     * Callback for Location events.
//     */
//    private LocationCallback mLocationCallback;
//    /**
//     * Represents a geographical location.
//     */
//    private Location mCurrentLocation;
//    /**
//     * Tracks the status of the location updates request. Value changes when the user presses the
//     * Start Updates and Stop Updates buttons.
//     */
//    private Boolean mRequestingLocationUpdates;
//    /**
//     * Time when the location was updated represented as a String.
//     */
//    private String mLastUpdateTime;
//    private GoogleMap mMap;
//    private GoogleApiClient mGoogleApiClient;
//    private CreateViewModel createViewModel;
//    private ArrayList<Marker> markerArrayList = new ArrayList<>();
//
//    public static LocationFragment create(String owner, String name) {
//        LocationFragment repoFragment = new LocationFragment();
//        Bundle args = new Bundle();
//        args.putString(REPO_OWNER_KEY, owner);
//        args.putString(REPO_NAME_KEY, name);
//        repoFragment.setArguments(args);
//        return repoFragment;
//    }
//
//    @Override
//    public LifecycleRegistry getLifecycle() {
//        return lifecycleRegistry;
//    }
//
//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        createViewModel = ViewModelProviders.of(this, viewModelFactory).get(CreateViewModel.class);
//        initStartUpdatesListener();
//        getActivity().setTitle("Create Story");
//
//
//        // Create an instance of GoogleAPIClient.
//        if (mGoogleApiClient == null) {
//            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build();
//        }
//
//        // Location
//        super.onCreate(savedInstanceState);
//
//        // Set labels.
//
//        mRequestingLocationUpdates = false;
//        mLastUpdateTime = "";
//
//        // Update values using data stored in the Bundle.
//        updateValuesFromBundle(savedInstanceState);
//
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
//        mSettingsClient = LocationServices.getSettingsClient(getContext());
//
//        // Kick off the process of building the LocationCallback, LocationRequest, and
//        // LocationSettingsRequest objects.
//        createLocationCallback();
//        createLocationRequest();
//        buildLocationSettingsRequest();
//    }
//
//    /**
//     * Handles the Start Updates button and requests start of location updates. Does nothing if
//     * updates have already been requested.
//     */
//    private void initStartUpdatesListener() {
//        if (!mRequestingLocationUpdates) {
//            mRequestingLocationUpdates = true;
//
//            startLocationUpdates();
//        }
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//
//        SupportMapFragment mapFragment =
//                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//
//
//        return null;
//    }
//
//
//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, TAKE_PICTURE_REQUEST_CODE);
//        }
//    }
//
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        updateLocation();
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//    }
//
//    @Override
//    public void onStart() {
//        mGoogleApiClient.connect();
//        super.onStart();
//    }
//
//    @Override
//    public void onStop() {
//        mGoogleApiClient.disconnect();
//        super.onStop();
//    }
//
//    public void updateLocation() {
//        if (ActivityCompat.checkSelfPermission(getContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
//                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//        }
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        // Set map preferences
//        mMap.setMinZoomPreference(10.0f);
//        mMap.setMaxZoomPreference(16.0f);
//    }
//
//    /**
//     * Updates fields based on data stored in the bundle.
//     *
//     * @param savedInstanceState The activity state saved in the Bundle.
//     */
//    private void updateValuesFromBundle(Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
//            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
//            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
//                mRequestingLocationUpdates = savedInstanceState.getBoolean(
//                        KEY_REQUESTING_LOCATION_UPDATES);
//            }
//
//            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
//            // correct latitude and longitude.
//            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
//                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
//                // is not null.
//                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
//            }
//
//            // Update the value of mLastUpdateTime from the Bundle and update the UI.
//            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
//                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
//            }
//        }
//    }
//
//    /**
//     * Sets up the location request. Android has two location request settings:
//     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
//     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
//     * the AndroidManifest.xml.
//     * <p/>
//     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
//     * interval (5 seconds), the Fused Location Provider API returns location updates that are
//     * accurate to within a few feet.
//     * <p/>
//     * These settings are appropriate for mapping applications that show real-time location
//     * updates.
//     */
//    private void createLocationRequest() {
//        mLocationRequest = new LocationRequest();
//
//        // Sets the desired interval for active location updates. This interval is
//        // inexact. You may not receive updates at all if no location sources are available, or
//        // you may receive them slower than requested. You may also receive updates faster than
//        // requested if other applications are requesting location at a faster interval.
//        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
//
//        // Sets the fastest rate for active location updates. This interval is exact, and your
//        // application will never receive updates faster than this value.
//        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
//
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//    }
//
//    /**
//     * Creates a callback for receiving location events.
//     */
//    private void createLocationCallback() {
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                super.onLocationResult(locationResult);
//                Log.i("current location", locationResult.getLastLocation().toString());
//                mCurrentLocation = locationResult.getLastLocation();
//                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
//
//            }
//        };
//    }
//
//    /**
//     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
//     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
//     * if a device has the needed location settings.
//     */
//    private void buildLocationSettingsRequest() {
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(mLocationRequest);
//        mLocationSettingsRequest = builder.build();
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            // Check for the integer request code originally supplied to startResolutionForResult().
//            case REQUEST_CHECK_SETTINGS:
//                switch (resultCode) {
//                    case Activity.RESULT_OK:
//                        Log.i(TAG, "User agreed to make required location settings changes.");
//                        // Nothing to do. startLocationupdates() gets called in onResume again.
//                        break;
//                    case Activity.RESULT_CANCELED:
//                        Log.i(TAG, "User chose not to make required location settings changes.");
//                        mRequestingLocationUpdates = false;
//                        break;
//                }
//                break;
//        }
//        if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            createViewModel.addExposition(ExpositionType.PICTURE, imageBitmap.toString());
//            adapter.get().notifyItemChanged(createViewModel.getAllChapters().size() - 1);
//
//        } else if (requestCode == RECORD_AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {
//            Uri audioUri = data.getData();
//            createViewModel.addExposition(ExpositionType.AUDIO, audioUri.toString());
//            adapter.get().notifyItemChanged(createViewModel.getAllChapters().size() - 1);
//
//
//        }
//    }
//
//
//    /**
//     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
//     * runtime permission has been granted.
//     */
//    @SuppressWarnings("MissingPermission")
//    private void startLocationUpdates() {
//        // Begin by checking if the device has the necessary location settings.
//        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
//                .addOnSuccessListener(getActivity(), locationSettingsResponse -> {
//                    Log.i(TAG, "All location settings are satisfied.");
//
//                    //noinspection MissingPermission
//                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
//                            mLocationCallback, Looper.myLooper());
//
//                })
//                .addOnFailureListener(getActivity(), e -> {
//                    int statusCode = ((ApiException) e).getStatusCode();
//                    switch (statusCode) {
//                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                            Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
//                                    "location settings ");
//                            try {
//                                // Show the dialog by calling startResolutionForResult(), and check the
//                                // result in onActivityResult().
//                                ResolvableApiException rae = (ResolvableApiException) e;
//                                rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
//                            } catch (IntentSender.SendIntentException sie) {
//                                Log.i(TAG, "PendingIntent unable to execute request.");
//                            }
//                            break;
//                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                            String errorMessage = "Location settings are inadequate, and cannot be " +
//                                    "fixed here. Fix in Settings.";
//                            Log.e(TAG, errorMessage);
//                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
//                            mRequestingLocationUpdates = false;
//                    }
//
//                });
//    }
//
//    /**
//     * Removes location updates from the FusedLocationApi.
//     */
//    private void stopLocationUpdates() {
//        if (!mRequestingLocationUpdates) {
//            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
//            return;
//        }
//
//        // It is a good practice to remove location requests when the activity is in a paused or
//        // stopped state. Doing so helps battery performance and is especially
//        // recommended in applications that request frequent location updates.
//        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
//                .addOnCompleteListener(getActivity(), task -> {
//                    mRequestingLocationUpdates = false;
//                });
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
//        // location updates if the user has requested them.
//        if (mRequestingLocationUpdates && checkPermissions()) {
//            startLocationUpdates();
//        } else if (!checkPermissions()) {
//            requestPermissions();
//        }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//
//        // Remove location updates to save battery.
//        stopLocationUpdates();
//    }
//
//    /**
//     * Stores activity data in the Bundle.
//     */
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
//        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
//        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
//        super.onSaveInstanceState(savedInstanceState);
//    }
//
//    /**
//     * Shows a {@link Snackbar}.
//     *
//     * @param locationFragment
//     * @param mainTextStringId The id for the string resource for the Snackbar text.
//     * @param actionStringId   The text of the action item.
//     * @param listener         The listener associated with the Snackbar action.
//     */
//    public void showSnackbar(LocationFragment locationFragment, final int mainTextStringId, final int actionStringId,
//                             View.OnClickListener listener) {
//        Snackbar.make(
//                locationFragment.getActivity().findViewById(android.R.id.content),
//                locationFragment.getString(mainTextStringId),
//                Snackbar.LENGTH_INDEFINITE)
//                .setAction(locationFragment.getString(actionStringId), listener).show();
//    }
//
//    /**
//     * Return the current state of the permissions needed.
//     */
//    private boolean checkPermissions() {
//        int permissionState = ActivityCompat.checkSelfPermission(getContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION);
//        return permissionState == PackageManager.PERMISSION_GRANTED;
//    }
//
//
//}
