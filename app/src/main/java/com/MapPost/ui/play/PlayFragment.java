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

package com.MapPost.ui.play;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.MapPost.R;
import com.MapPost.aws.ConstantsKt;
import com.MapPost.binding.FragmentDataBindingComponent;
import com.MapPost.databinding.BottomSheetChapterListBinding;
import com.MapPost.databinding.FragmentPlayBinding;
import com.MapPost.di.Injectable;
import com.MapPost.repository.tasks.StoryKey;
import com.MapPost.ui.common.BetterSnapper;
import com.MapPost.ui.common.ChapterAdapter;
import com.MapPost.ui.common.LocationLiveData;
import com.MapPost.ui.common.LocationUtilKt;
import com.MapPost.ui.common.NavigationController;
import com.MapPost.util.AutoClearedValue;
import com.MapPost.vo.Chapter;
import com.MapPost.vo.Post;
import com.MapPost.vo.Story;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.inject.Inject;


public class PlayFragment extends Fragment implements
        Injectable,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private static final String REPO_NAME_KEY = "repo_name";
    private static final String REPO_USER_ID_KEY = "repo_userid";
    private static final int MARKER_HEIGHT = 100;
    private static final int MARKER_WIDTH = 100;
    private static final String TAG = PlayFragment.class.getSimpleName();
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<FragmentPlayBinding> binding;
    AutoClearedValue<ChapterAdapter> adapter;
    BottomSheetChapterListBinding bottomSheet;
    private PlayViewModel playViewModel;
    private GoogleMap mMap;
    private FloatingActionButton nextChapterButton;
    private ArrayList<Marker> markers = new ArrayList<>();
    private Story story;
    private Random mRandom = new Random(1984);

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
        double distance = distanceBetween(location, LocationUtilKt.latLngToLocation(nextChapter.getLocation()));
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
        bottomSheet.expositionList.setAdapter(adapter);
        nextChapterButton = binding.get().nextChapter;
        new BetterSnapper().attachToRecyclerView(bottomSheet.expositionList);

        initCurrentChapterObserver();
        initLocationObserver();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentPlayBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_play, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return dataBinding.getRoot();
    }

    private void initCurrentChapterObserver() {
        playViewModel.getPosts().observe(this, listResource -> {
            if (listResource == null || listResource.data == null) return;

            for (Post post : listResource.data) {

                IconGenerator iconGenerator = new IconGenerator(getContext());
                ImageView imageView = new ImageView(getContext());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(MARKER_WIDTH, MARKER_HEIGHT));
                MarkerOptions markerOptions;
                LatLng location = new LatLng(post.getLatitude(), post.getLongitude());

                switch (post.getType()) {
                    case TEXT:
                        imageView.setImageResource(R.drawable.ic_textsms_black_24dp);
                        iconGenerator.setContentView(imageView);
                        markerOptions = new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                                .position(location);
                        markers.add(mMap.addMarker(markerOptions));
                        break;
                    case AUDIO:
                        imageView.setImageResource(R.drawable.ic_audiotrack_black_24dp);
                        iconGenerator.setContentView(imageView);
                        markerOptions = new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                                .position(location);
                        markers.add(mMap.addMarker(markerOptions));
                        break;
                    case PICTURE:
                        Glide.with(getContext())
                                .asBitmap()
                                .load(ConstantsKt.getS3HostName() + post.getContent())
                                .apply(new RequestOptions().centerCrop())
                                .into(new SimpleTarget<Bitmap>(MARKER_WIDTH, MARKER_HEIGHT) {
                                    @Override
                                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                        imageView.setImageBitmap(resource);
                                        iconGenerator.setContentView(imageView);
                                        MarkerOptions markerOptions = new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                                                .position(location);
                                        markers.add(mMap.addMarker(markerOptions));
                                    }
                                });
                        break;
                }
            }

        });
    }

    private void initLocationObserver() {
        new LocationLiveData(getContext()).observe(this, location -> {

        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set map preferences
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));
        mMap.setMyLocationEnabled(true);
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
        for (int i = 0; i < markers.size(); ++i) {
            if (markers.get(i).equals(marker)) {
                List<Chapter> chapters = story.chapters;
                for (int chapter = 0; chapter < chapters.size(); chapter++) {
                    for (Post post : chapters.get(chapter).getPosts()) {
                        if (Objects.equals(post.getPostId(), "" + i)) {
                            BottomSheetBehavior.from(bottomSheet.bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                            bottomSheet.expositionList.smoothScrollToPosition(chapter);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
