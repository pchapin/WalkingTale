/*
  Copyright 2017 Google Inc. All Rights Reserved.
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.android.example.github.ui.play;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.FragmentPlayBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.ChapterAdapter;
import com.android.example.github.ui.common.LocationLiveData;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.util.AutoClearedValue;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;
import com.android.example.github.walkingTale.LocationUtilKt;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collections;
import java.util.Date;

import javax.inject.Inject;

public class PlayFragment extends Fragment implements LifecycleRegistryOwner, Injectable, OnMapReadyCallback {

    private static final String REPO_NAME_KEY = "repo_name";
    private final String TAG = this.getClass().getSimpleName();
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<FragmentPlayBinding> binding;
    AutoClearedValue<ChapterAdapter> adapter;
    private PlayViewModel playViewModel;
    private Location mCurrentLocation;
    private boolean userInNextChapterRadius = false;
    private GoogleMap mMap;


    public static PlayFragment create(String id) {
        PlayFragment repoFragment = new PlayFragment();
        Bundle args = new Bundle();
        args.putString(REPO_NAME_KEY, id);
        repoFragment.setArguments(args);
        return repoFragment;
    }

    /**
     * Distance in meters between two locations
     */
    private static double distanceBetween(Location location1, Location location2) {
        float[] distanceBetween = new float[1];

        Location.distanceBetween(
                location1.getLatitude(),
                location1.getLongitude(),
                location2.getLatitude(),
                location2.getLongitude(),
                distanceBetween);
        return distanceBetween[0];
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        playViewModel = ViewModelProviders.of(this, viewModelFactory).get(PlayViewModel.class);
        Bundle args = getArguments();
        playViewModel.setId(args.getString(REPO_NAME_KEY));

        LiveData<Resource<Repo>> repo = playViewModel.getRepo();
        repo.observe(this, resource -> {
            binding.get().setRepo(resource == null ? null : resource.data);
            binding.get().setRepoResource(resource);
            binding.get().executePendingBindings();
            if (!playViewModel.isStorySet() && resource != null && resource.data != null) {
                playViewModel.setStory(resource.data);
            }
        });

        // When location changes, call a method with the location
        new LocationLiveData(getContext()).observe(this, this::locationChangeListener);

        ChapterAdapter adapter = new ChapterAdapter(dataBindingComponent,
                chapter -> {
                });
        this.adapter = new AutoClearedValue<>(this, adapter);
        binding.get().expositionList.setAdapter(adapter);

        initViewExpositionsListener();
        initNextChapterListener();
        initExpositionList();

        // Disable next chapter button until user is in radius
        userInNextChapterRadius = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentPlayBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_play, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return dataBinding.getRoot();
    }

    private void locationChangeListener(Location currentLocation) {
        mCurrentLocation = currentLocation;
        binding.get().latitudeText.setText(String.format("%s", mCurrentLocation.getLatitude()));
        binding.get().longitudeText.setText(String.format("%s", mCurrentLocation.getLongitude()));
        binding.get().lastUpdateTimeText.setText(new Date().toString());
        isUserInRadius();
        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newLatLng(LocationUtilKt.LocationToLatLng(mCurrentLocation));
        mMap.animateCamera(cameraUpdate);
    }

    private void initViewExpositionsListener() {
        ToggleButton toggle = binding.get().viewExpositions;
        toggle.setTextOn("Hide Expositions");
        toggle.setTextOff("View Expositions");
        RecyclerView expositionList = binding.get().expositionList;
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                expositionList.setVisibility(View.VISIBLE);
            } else {
                expositionList.setVisibility(View.GONE);
            }
        });
        toggle.performClick();
        toggle.performClick();
    }

    private void initNextChapterListener() {
        binding.get().nextChapter.setOnClickListener((v) -> {
            if (userInNextChapterRadius) {
                nextChapterEvent();
                binding.get().nextChapter.setEnabled(false);
            } else {
                Toast.makeText(getContext(), "You are not in the radius", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initExpositionList() {
        playViewModel.availableChapters.observe(this, listResource -> {
            if (listResource != null) {
                adapter.get().replace(listResource);
            } else {
                adapter.get().replace(Collections.emptyList());
            }
        });
    }

    /**
     * Check if user is within next chapter radius
     */
    private void isUserInRadius() {
        LatLng latLng = playViewModel.getNextChapter().getLocation();
        float[] distanceBetween = new float[1];

        // Distance in meters from here to center of chapter radius
        Location.distanceBetween(
                mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(),
                latLng.latitude,
                latLng.longitude,
                distanceBetween);
        userInNextChapterRadius = distanceBetween[0] < playViewModel.getNextChapter().getRadius();
        binding.get().setIsUserInNextChapterRadius(userInNextChapterRadius);
    }

    private void nextChapterEvent() {
        try {
            playViewModel.goToNextChapter();
            Toast.makeText(getContext(), "current chapter is now: " + playViewModel
                    .getNextChapter().getId(), Toast.LENGTH_SHORT).show();

            // Show chapter id + 1 on marker
            MarkerOptions markerOptions = new MarkerOptions()
                    .title("" + playViewModel.getNextChapter().getId())
                    .position(playViewModel.getNextChapter().getLocation());
            mMap.addMarker(markerOptions);

        } catch (ArrayIndexOutOfBoundsException e) {
            Toast.makeText(getContext(), "No more chapters!", Toast.LENGTH_SHORT).show();
            finalChapterEvent();
        }
    }

    private void finalChapterEvent() {
        new AlertDialog.Builder(getContext())
                .setTitle("Finish Story")
                .setMessage("Do you want to finish the story?")
                .setPositiveButton("yes", (dialogInterface, i) -> getActivity().onBackPressed())
                .setNegativeButton("no", (dialogInterface, i) -> {
                })
                .create().show();
    }

    @NonNull
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set map preferences
        mMap = googleMap;
        mMap.setMinZoomPreference(15.0f);
        mMap.setMaxZoomPreference(18.0f);

        // Change tilt
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mMap.getCameraPosition().target)
                .tilt(60).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setMapToolbarEnabled(false);
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setRotateGesturesEnabled(false);
        mUiSettings.setCompassEnabled(false);
    }
}
