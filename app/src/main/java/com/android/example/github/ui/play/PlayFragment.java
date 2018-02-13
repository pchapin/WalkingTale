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
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
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
import com.android.example.github.walkingTale.Chapter;
import com.android.example.github.walkingTale.Exposition;
import com.android.example.github.walkingTale.LocationUtilKt;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.Collections;

import javax.inject.Inject;


public class PlayFragment extends Fragment implements
        LifecycleRegistryOwner,
        Injectable,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCircleClickListener {

    private static final String REPO_NAME_KEY = "repo_name";
    private static final String TAG = PlayFragment.class.getSimpleName();
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<FragmentPlayBinding> binding;
    AutoClearedValue<ChapterAdapter> adapter;
    private PlayViewModel playViewModel;
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
    private static double distanceBetween(@NonNull Location location1, @NonNull Location location2) {
        float[] distanceBetween = new float[1];

        Location.distanceBetween(
                location1.getLatitude(),
                location1.getLongitude(),
                location2.getLatitude(),
                location2.getLongitude(),
                distanceBetween);
        return distanceBetween[0];
    }

    private static boolean isUserInNextRadius(@NonNull Location location, @Nullable Chapter nextChapter) {
        // User is not in radius of chapter that does not exist
        if (nextChapter == null) return false;
        double distance = distanceBetween(location, LocationUtilKt.LatLngToLocation(nextChapter.getLocation()));
        return distance < nextChapter.getRadius();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        playViewModel = ViewModelProviders.of(this, viewModelFactory).get(PlayViewModel.class);
        Bundle args = getArguments();
        playViewModel.setId(args.getString(REPO_NAME_KEY));

        ChapterAdapter adapter = new ChapterAdapter(dataBindingComponent,
                chapter -> {
                });
        this.adapter = new AutoClearedValue<>(this, adapter);
        binding.get().expositionList.setAdapter(adapter);

        initCurrentChapterObserver();
        initRepoObserver();
        initLocationObserver();
        initIsCurrentFinalObserver();

        initViewExpositionsListener();
        initNextChapterListener();
        initExpositionList();
        initFinishStoryListener();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentPlayBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_play, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return dataBinding.getRoot();
    }

    private void initCurrentChapterObserver() {
        playViewModel.getCurrentChapter().observe(this, chapter -> {
            if (chapter == null) return;

            for (Exposition exposition : chapter.getExpositions()) {
                IconGenerator iconGenerator = new IconGenerator(getContext());
                MarkerOptions marker = new MarkerOptions()
                        .position(chapter.getLocation())
                        .icon(BitmapDescriptorFactory
                                .fromBitmap(iconGenerator.makeIcon("" + exposition.getId())));
                mMap.addMarker(marker);
            }

            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(chapter.getLocation())
                    .clickable(true)
                    .radius(chapter.getRadius())
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.RED));
        });
    }

    private void initRepoObserver() {
        playViewModel.getRepo().observe(this, resource -> {
            binding.get().setRepo(resource == null ? null : resource.data);
            binding.get().setRepoResource(resource);
            binding.get().executePendingBindings();
            if (!playViewModel.isStorySet() && resource != null && resource.data != null) {
                playViewModel.setStory(resource.data);
            }
        });
    }

    private void initLocationObserver() {
        new LocationLiveData(getContext()).observe(this, location -> {
            moveCamera(location);
            boolean isUserInNext = isUserInNextRadius(location, playViewModel.getNextChapter().getValue());
            binding.get().nextChapter.setEnabled(isUserInNext);
        });
    }

    private void initIsCurrentFinalObserver() {
        playViewModel.getIsCurrentFinal().observe(this, isFinalChapter -> {
            if (isFinalChapter != null) {
                binding.get().setIsCurrentChapterFinal(isFinalChapter);
            }
        });
    }

    private void initViewExpositionsListener() {
        RecyclerView expositionList = binding.get().expositionList;
        ToggleButton toggle = binding.get().viewExpositions;
        toggle.setTextOn("Hide Expositions");
        toggle.setTextOff("View Expositions");
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
            if (!playViewModel.incrementChapter()) {
                Toast.makeText(getContext(), "No more chapters!", Toast.LENGTH_SHORT).show();
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

    private void initFinishStoryListener() {
        binding.get().finishPlayStoryBtn.setOnClickListener(view -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Finish Story")
                    .setMessage("Do you want to finish the story?")
                    .setPositiveButton("yes", (dialogInterface, i) -> getActivity().onBackPressed())
                    .setNegativeButton("no", (dialogInterface, i) -> {
                    })
                    .create().show();
        });
    }

    private void moveCamera(Location currentLocation) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(LocationUtilKt.LocationToLatLng(currentLocation));
        mMap.animateCamera(cameraUpdate);
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
        mMap.setMinZoomPreference(16.0f);
        mMap.setMaxZoomPreference(18.0f);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));
        mMap.setOnMarkerClickListener(this);

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

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(getContext(), "marker id " + marker.getId(), Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onCircleClick(Circle circle) {
        Toast.makeText(getContext(), "circle id " + circle.getId(), Toast.LENGTH_SHORT).show();
    }
}
