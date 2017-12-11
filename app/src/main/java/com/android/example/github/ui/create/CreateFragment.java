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

package com.android.example.github.ui.create;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.CreateFragmentBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.audiorecord.AudioRecordActivity;
import com.android.example.github.ui.common.ChapterAdapter;
import com.android.example.github.ui.common.LocationLiveData;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.util.AutoClearedValue;
import com.android.example.github.vo.Repo;
import com.android.example.github.walkingTale.Chapter;
import com.android.example.github.walkingTale.ExpositionType;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;


/**
 * The UI Controller for creating a story.
 */
public class CreateFragment extends Fragment implements
        LifecycleRegistryOwner,
        Injectable,
        OnMapReadyCallback {

    public static final String AUDIO_KEY_CHAPTER = "AUDIO_KEY_CHAPTER";
    public static final String AUDIO_KEY_EXPOSITION = "AUDIO_KEY_EXPOSITION";
    private final int RECORD_AUDIO_REQUEST_CODE = 123;
    private final int TAKE_PICTURE_REQUEST_CODE = 1234;
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<CreateFragmentBinding> binding;
    AutoClearedValue<ChapterAdapter> adapter;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;
    private GoogleMap mMap;
    private CreateViewModel createViewModel;
    private ArrayList<Marker> markerArrayList = new ArrayList<>();

    @NonNull
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        createViewModel = ViewModelProviders.of(this, viewModelFactory).get(CreateViewModel.class);
        initAddChapterListener();
        initRemoveChapterListener();
        initFinishStoryListener();
        initAddTextListener();
        initAddPictureListener();
        initAddAudioListener();
        initRadiusIncrementListener();
        initRadiusDecrementListener();
        initContributorList(createViewModel);
        getActivity().setTitle("Create Story");

        LiveData<Repo> repo = createViewModel.getStory();
        repo.observe(this, resource -> {
            binding.get().setRepo(resource);
            binding.get().executePendingBindings();
        });

        LocationLiveData liveData = new LocationLiveData(getContext());
        liveData.observe(this, currentLocation -> {
            mCurrentLocation = currentLocation;
            binding.get().latitudeText.setText(Double.toString(mCurrentLocation.getLatitude()));
            binding.get().longitudeText.setText(Double.toString(mCurrentLocation.getLongitude()));
            binding.get().lastUpdateTimeText.setText(new Date().toString());
            Log.i("location", "" + mCurrentLocation + new Date().toString());
        });

        createViewModel.getIsPublishSuccessful().observe(this, isSuccessful -> {
            binding.get().isPublishSuccessful.setText("Is publish successful: " + isSuccessful);
        });

        ChapterAdapter adapter = new ChapterAdapter(dataBindingComponent,
                chapter -> navigationController.navigateToExpositionViewer(repo.getValue().id));
        this.adapter = new AutoClearedValue<>(this, adapter);
        binding.get().chapterList.setAdapter(adapter);
    }

    private void initAddChapterListener() {
        binding.get().addChapterButton.setOnClickListener((v) -> {

            // TODO: 11/22/17 DEBUG: if location is null, prevent user from using app. Location is required
            if (mCurrentLocation == null) {
                mCurrentLocation = new Location("");
                mCurrentLocation.setLatitude(1.1);
                mCurrentLocation.setLongitude(2.2);
            }

            // TODO: 10/27/2017 get chapter name from the author
            String chapterName = "Chapter Name";
            createViewModel.addChapter(chapterName,
                    new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 1);

            // Add marker to map
            LatLng chapterLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

//            todo: fix map shrinking
//            Marker newMarker = mMap.addMarker(new MarkerOptions()
//                    .position(chapterLocation)
//                    .title(chapterName));
//            markerArrayList.add(newMarker);
//
//            // Get bounds of all markers
//            LatLngBounds.Builder builder = new LatLngBounds.Builder();
//            for (Marker marker : markerArrayList) {
//                builder.include(marker.getPosition());
//            }
//            LatLngBounds bounds = builder.build();
//
//            int padding = 0; // offset from edges of the map in pixels
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//            mMap.animateCamera(cameraUpdate);

            adapter.get().notifyItemInserted(createViewModel.getAllChapters().size() - 1);
        });
    }

    private void initRemoveChapterListener() {
        binding.get().removeChapterButton.setOnClickListener((v) -> {
            try {
                adapter.get().notifyItemRemoved(createViewModel.getAllChapters().size() - 1);
                createViewModel.removeChapter();
                // Remove marker from map and list
                markerArrayList.get(markerArrayList.size() - 1).remove();
                markerArrayList.remove(markerArrayList.size() - 1);
            } catch (ArrayIndexOutOfBoundsException e) {
                Toast.makeText(getContext(), "No chapters to remove.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initFinishStoryListener() {
        binding.get().finishStoryButton.setOnClickListener((v) -> {
            if (createViewModel.getAllChapters().size() < 2) {
                Toast.makeText(getContext(), "Your story must have at least 2 chapters.", Toast.LENGTH_SHORT).show();
            } else {
                createViewModel.finishStory();
            }
        });
    }

    private void initAddTextListener() {
        binding.get().addTextExpositionButton.setOnClickListener((v) -> {
            try {
                createViewModel.addExposition(ExpositionType.TEXT, "hello world");
                adapter.get().notifyItemChanged(createViewModel.getAllChapters().size() - 1);
            } catch (NoSuchElementException e) {
                Toast.makeText(getContext(), "No chapters to add expositions to.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initAddPictureListener() {
        binding.get().addPictureExpositionButton.setOnClickListener((v) -> {
            if (createViewModel.getAllChapters().isEmpty()) {
                Toast.makeText(getContext(), "No chapters to add expositions to.", Toast.LENGTH_SHORT).show();
            } else {
                dispatchTakePictureIntent();
            }
        });
    }

    private void initAddAudioListener() {
        binding.get().addAudioExpositionButton.setOnClickListener((v) -> {
            if (createViewModel.getAllChapters().isEmpty()) {
                Toast.makeText(getContext(), "No chapters to add expositions to.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getActivity(), AudioRecordActivity.class);
                intent.setType("audio/mpeg4-generic");
                Chapter latestChapter = createViewModel.getLatestChapter();
                Bundle bundle = new Bundle();
                bundle.putString(AUDIO_KEY_CHAPTER, String.valueOf(latestChapter.getId()));
                bundle.putString(AUDIO_KEY_EXPOSITION, Integer.toString(latestChapter.getExpositions().size()));
                intent.putExtras(bundle);
                startActivityForResult(intent, RECORD_AUDIO_REQUEST_CODE);
            }
        });
    }

    private void initRadiusIncrementListener() {
        binding.get().radiusIncrementButton.setOnClickListener((v) -> {
            try {
                createViewModel.incrementRadius();
                adapter.get().notifyItemChanged(createViewModel.getAllChapters().size() - 1);
            } catch (NoSuchElementException e) {
                Toast.makeText(getContext(), "No chapters to increment radius.", Toast.LENGTH_SHORT).show();
            } catch (ArrayIndexOutOfBoundsException e) {
                Toast.makeText(getContext(), "Radius is already at max size.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initRadiusDecrementListener() {
        binding.get().radiusDecrementButton.setOnClickListener((v) -> {
            try {
                createViewModel.decrementRadius();
                adapter.get().notifyItemChanged(createViewModel.getAllChapters().size() - 1);
            } catch (NoSuchElementException e) {
                Toast.makeText(getContext(), "No chapters to decrement radius.", Toast.LENGTH_SHORT).show();
            } catch (ArrayIndexOutOfBoundsException e) {
                Toast.makeText(getContext(), "Radius is already at min size.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initContributorList(CreateViewModel viewModel) {
        viewModel.getStory().observe(this, listResource -> {
            if (listResource != null) {
                adapter.get().replace(listResource.chapters);
            } else {
                adapter.get().replace(Collections.emptyList());
            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        CreateFragmentBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.create_fragment, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);

        //todo: fix bug where map shrinks
        //        SupportMapFragment mapFragment =
        //                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        //        mapFragment.getMapAsync(this);

        return dataBinding.getRoot();
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_PICTURE_REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Set map preferences
        mMap.setMinZoomPreference(10.0f);
        mMap.setMaxZoomPreference(16.0f);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            createViewModel.addExposition(ExpositionType.PICTURE, imageBitmap.toString());
            adapter.get().notifyItemChanged(createViewModel.getAllChapters().size() - 1);

        } else if (requestCode == RECORD_AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri audioUri = data.getData();
            createViewModel.addExposition(ExpositionType.AUDIO, audioUri.toString());
            adapter.get().notifyItemChanged(createViewModel.getAllChapters().size() - 1);
        }
    }
}
