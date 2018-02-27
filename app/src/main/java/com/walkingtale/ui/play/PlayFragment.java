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

package com.walkingtale.ui.play;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.walkingtale.R;
import com.walkingtale.binding.FragmentDataBindingComponent;
import com.walkingtale.databinding.FragmentPlayBinding;
import com.walkingtale.di.Injectable;
import com.walkingtale.repository.tasks.StoryKey;
import com.walkingtale.ui.common.ChapterAdapter;
import com.walkingtale.ui.common.LocationLiveData;
import com.walkingtale.ui.common.LocationUtilKt;
import com.walkingtale.ui.common.NavigationController;
import com.walkingtale.util.AutoClearedValue;
import com.walkingtale.vo.Chapter;
import com.walkingtale.vo.Exposition;
import com.walkingtale.vo.Story;

import java.util.Collections;

import javax.inject.Inject;


public class PlayFragment extends Fragment implements
        Injectable,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCircleClickListener {

    private static final String REPO_NAME_KEY = "repo_name";
    private static final String REPO_USER_ID_KEY = "repo_userid";
    private static final String TAG = PlayFragment.class.getSimpleName();
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<FragmentPlayBinding> binding;
    AutoClearedValue<ChapterAdapter> adapter;
    private PlayViewModel playViewModel;
    private GoogleMap mMap;
    private FloatingActionButton nextChapterButton;

    public static PlayFragment create(Story story) {
        PlayFragment repoFragment = new PlayFragment();
        Bundle args = new Bundle();
        args.putString(REPO_NAME_KEY, story.id);
        args.putString(REPO_USER_ID_KEY, story.userId);
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
        StoryKey storyKey = new StoryKey(args.getString(REPO_USER_ID_KEY), args.getString(REPO_NAME_KEY));

        ChapterAdapter adapter = new ChapterAdapter(dataBindingComponent,
                chapter -> {
                });
        this.adapter = new AutoClearedValue<>(this, adapter);
        binding.get().bottomSheetList.expositionList.setAdapter(adapter);
        nextChapterButton = binding.get().nextChapter;

        initCurrentChapterObserver();
        initStoryObserver(storyKey);
        initLocationObserver();
        initIsCurrentFinalObserver();
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

    private void initStoryObserver(StoryKey storyKey) {
        playViewModel.getStory(storyKey).observe(this, resource -> {
            binding.get().setStory(resource == null ? null : resource.data);
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
            nextChapterButton.setEnabled(isUserInNext);
        });
    }

    private void initIsCurrentFinalObserver() {
        playViewModel.getIsCurrentFinal().observe(this, isFinalChapter -> {
            if (isFinalChapter != null) {
                binding.get().setIsCurrentChapterFinal(isFinalChapter);
            }
        });
    }

    private void initNextChapterListener() {
        nextChapterButton.setOnClickListener((v) -> {
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set map preferences
        mMap = googleMap;
        mMap.setMinZoomPreference(18.0f);
        mMap.setMaxZoomPreference(20.0f);
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
