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

import android.Manifest;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.PlayFragmentBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.ChapterAdapter;
import com.android.example.github.ui.common.LocationLiveData;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.util.AutoClearedValue;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;
import com.android.example.github.walkingTale.StoryPlayManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.Date;

import javax.inject.Inject;

public class PlayFragment extends Fragment implements LifecycleRegistryOwner, Injectable, OnMapReadyCallback {

    private static final String REPO_NAME_KEY = "repo_name";
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<PlayFragmentBinding> binding;
    AutoClearedValue<ChapterAdapter> adapter;
    StoryPlayManager storyPlayManager;
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        playViewModel = ViewModelProviders.of(this, viewModelFactory).get(PlayViewModel.class);
        Bundle args = getArguments();
        if (args != null && args.containsKey(REPO_NAME_KEY)) {
            playViewModel.setId(args.getString(REPO_NAME_KEY));
        } else {
            playViewModel.setId(null);
        }
        LiveData<Resource<Repo>> repo = playViewModel.getRepo();
        repo.observe(this, resource -> {
            binding.get().setRepo(resource == null ? null : resource.data);
            binding.get().setRepoResource(resource);
            binding.get().executePendingBindings();
            if (storyPlayManager == null && resource != null && resource.data != null) {
                storyPlayManager = new StoryPlayManager(resource.data);
            }
        });

        LocationLiveData locationLiveData = new LocationLiveData(getContext());
        locationLiveData.observe(this, currentLocation -> {
            mCurrentLocation = currentLocation;
            binding.get().latitudeText.setText(Double.toString(mCurrentLocation.getLatitude()));
            binding.get().longitudeText.setText(Double.toString(mCurrentLocation.getLongitude()));
            binding.get().lastUpdateTimeText.setText(new Date().toString());
            Log.i("location", "" + mCurrentLocation + new Date().toString());
            isUserInRadius();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
            mMap.animateCamera(cameraUpdate);
        });

        ChapterAdapter adapter = new ChapterAdapter(dataBindingComponent,
                chapter -> navigationController.navigateToExpositionViewer(repo.getValue().data.id));
        this.adapter = new AutoClearedValue<>(this, adapter);
        binding.get().chapterList.setAdapter(adapter);
        initViewExpositionsListener();
        initNextChapterListener();
        initContributorList(playViewModel);
        getActivity().setTitle("Play Story");
        // Disable next chapter button until user is in radius
        userInNextChapterRadius = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        PlayFragmentBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.play_fragment, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return dataBinding.getRoot();
    }

    private void initViewExpositionsListener() {
        ToggleButton toggle = binding.get().viewExpositions;
        toggle.setTextOn("Hide Expositions");
        toggle.setTextOff("View Expositions");
        RecyclerView chapterList = binding.get().chapterList;
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chapterList.setVisibility(View.VISIBLE);
            } else {
                chapterList.setVisibility(View.GONE);
            }
        });
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

    private void initContributorList(PlayViewModel viewModel) {
        viewModel.getRepo().observe(this, listResource -> {
            if (listResource != null && listResource.data != null) {
                adapter.get().replace(listResource.data.chapters);
            } else {
                adapter.get().replace(Collections.emptyList());
            }
        });
    }

    /**
     * Check if user is within next chapter radius
     */
    private void isUserInRadius() {
        LatLng latLng = storyPlayManager.getCurrentChapter().getLocation();
        float[] distanceBetween = new float[1];

        // Distance in meters from here to center of chapter radius
        Location.distanceBetween(
                mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(),
                latLng.latitude,
                latLng.longitude,
                distanceBetween);
//        Toast.makeText(getContext(), "" + distanceBetween[0] + userInNextChapterRadius, Toast.LENGTH_SHORT).show();
        userInNextChapterRadius = distanceBetween[0] < storyPlayManager.getCurrentChapter().getRadius();
        binding.get().setIsUserInNextChapterRadius(userInNextChapterRadius);
    }

    private void nextChapterEvent() {
        try {
            storyPlayManager.goToNextChapter();
            Toast.makeText(getContext(), "current chapter is now:" + storyPlayManager.getCurrentChapter().toString(), Toast.LENGTH_SHORT).show();
        } catch (ArrayIndexOutOfBoundsException e) {
            Toast.makeText(getContext(), "No more chapters!", Toast.LENGTH_SHORT).show();
            finalChapterEvent();
        }
    }

    private void finalChapterEvent() {
        new AlertDialog.Builder(getContext())
                .setTitle("Finish Story")
                .setMessage("Do you want to finish the story?")
                .setPositiveButton("yes", (dialogInterface, i) -> {
                }).setNegativeButton("no", (dialogInterface, i) -> {
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
        mMap.setMinZoomPreference(10.0f);
        mMap.setMaxZoomPreference(16.0f);
    }
}
