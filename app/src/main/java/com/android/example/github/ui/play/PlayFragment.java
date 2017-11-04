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

package com.android.example.github.ui.play;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.PlayFragmentBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.ChapterAdapter;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.util.AutoClearedValue;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;
import com.android.example.github.walkingTale.Chapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.Executor;

import javax.inject.Inject;

/**
 * The UI Controller for displaying a Story being played by a reader.
 */
public class PlayFragment extends Fragment implements
        LifecycleRegistryOwner,
        Injectable,
        OnMapReadyCallback {

    // Google Maps
    private static final String TAG = PlayFragment.class.getSimpleName();
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String REPO_OWNER_KEY = "repo_owner";
    private static final String REPO_NAME_KEY = "repo_name";
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<PlayFragmentBinding> binding;
    AutoClearedValue<ChapterAdapter> adapter;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private GoogleMap mMap;
    private PlayViewModel playViewModel;

    public static PlayFragment create(String owner, String name) {
        PlayFragment repoFragment = new PlayFragment();
        Bundle args = new Bundle();
        args.putString(REPO_OWNER_KEY, owner);
        args.putString(REPO_NAME_KEY, name);
        repoFragment.setArguments(args);
        return repoFragment;
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        playViewModel = ViewModelProviders.of(this, viewModelFactory).get(PlayViewModel.class);
        Bundle args = getArguments();
        if (args != null && args.containsKey(REPO_OWNER_KEY) &&
                args.containsKey(REPO_NAME_KEY)) {
            playViewModel.setId(args.getString(REPO_OWNER_KEY),
                    args.getString(REPO_NAME_KEY));
        } else {
            playViewModel.setId(null, null);
        }
        LiveData<Resource<Repo>> repo = playViewModel.getRepo();
        repo.observe(this, resource -> {
            binding.get().setRepo(resource == null ? null : resource.data);
            binding.get().setRepoResource(resource);
            binding.get().executePendingBindings();
        });

        ChapterAdapter adapter = new ChapterAdapter(dataBindingComponent, false,
                chapter -> navigationController.navigateToExpositionViewer(repo.getValue().data.owner.login, repo.getValue().data.name));
        this.adapter = new AutoClearedValue<>(this, adapter);
        binding.get().chapterList.setAdapter(adapter);
        initViewExpositionsListener();
        initViewMapListener();
        initContributorList(playViewModel);
        getActivity().setTitle("Play Story");

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        PlayFragmentBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.play_fragment, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    private void initViewExpositionsListener() {
        binding.get().viewExpositions.setOnClickListener((v) -> {
            //Todo: Show all expositions
            String msg = "location is null";
            if (mLastKnownLocation != null) {
                msg = mLastKnownLocation.toString();
            }
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void initViewMapListener() {
        binding.get().viewMap.setOnClickListener((v) -> {
            //Todo: Open map
        });
    }

    private void initContributorList(PlayViewModel viewModel) {
        viewModel.getRepo().observe(this, listResource -> {
            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (listResource != null && listResource.data != null) {
                adapter.get().replace(listResource.data.chapters);
            } else {
                //noinspection ConstantConditions
                adapter.get().replace(Collections.emptyList());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getDeviceLocation();
        getLocationPermission();
        updateLocationUI();
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
//                            mMap.moveCamera(CameraUpdateFactory
//                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getContext().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}
