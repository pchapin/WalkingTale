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

package com.walkingtale.ui.create;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.walkingtale.R;
import com.walkingtale.binding.FragmentDataBindingComponent;
import com.walkingtale.databinding.FragmentCreateBinding;
import com.walkingtale.di.Injectable;
import com.walkingtale.ui.audiorecord.AudioRecordActivity;
import com.walkingtale.ui.common.ChapterAdapter;
import com.walkingtale.ui.common.FileUtilKt;
import com.walkingtale.ui.common.LocationLiveData;
import com.walkingtale.ui.common.LocationUtilKt;
import com.walkingtale.ui.common.NavigationController;
import com.walkingtale.util.AutoClearedValue;
import com.walkingtale.vo.Chapter;
import com.walkingtale.vo.ExpositionType;
import com.walkingtale.vo.Status;
import com.walkingtale.vo.Story;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;


/**
 * The UI Controller for creating a story.
 */
public class CreateFragment extends Fragment implements
        Injectable,
        OnMapReadyCallback {
    public static final String AUDIO_KEY_CHAPTER = "AUDIO_KEY_CHAPTER";
    public static final String AUDIO_KEY_EXPOSITION = "AUDIO_KEY_EXPOSITION";
    private final String TAG = this.getClass().getSimpleName();
    private final int RECORD_AUDIO_REQUEST_CODE = 123;
    private final int TAKE_EXPOSITION_PICTURE_REQUEST_CODE = 1234;
    private final int TAKE_STORY_PICTURE_REQUEST_CODE = 12345;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<FragmentCreateBinding> binding;
    AutoClearedValue<ChapterAdapter> adapter;
    private Location mCurrentLocation;
    private GoogleMap mMap;
    private CreateViewModel createViewModel;
    private ArrayList<Marker> markerArrayList = new ArrayList<>();

    // This file is used to hold images and audio temporarily
    private File photoFile = null;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        createViewModel = ViewModelProviders.of(this, viewModelFactory).get(CreateViewModel.class);
        initGenreSpinner();
        initStoryImageListener();
        initAddChapterListener();
        initRemoveChapterListener();
        initFinishStoryListener();
        initAddTextListener();
        initAddPictureListener();
        initAddAudioListener();
        initRadiusIncrementListener();
        initRadiusDecrementListener();
        initExpositionList(createViewModel);

        LiveData<Story> repo = createViewModel.getStory();
        repo.observe(this, resource -> {
            binding.get().setStory(resource);
            binding.get().executePendingBindings();
        });

        LocationLiveData liveData = new LocationLiveData(getContext());
        liveData.observe(this, currentLocation -> {
            mCurrentLocation = currentLocation;
            binding.get().latitudeText.setText(Double.toString(mCurrentLocation.getLatitude()));
            binding.get().longitudeText.setText(Double.toString(mCurrentLocation.getLongitude()));
            binding.get().lastUpdateTimeText.setText(new Date().toString());
        });

        ChapterAdapter adapter = new ChapterAdapter(dataBindingComponent,
                chapter -> navigationController.navigateToExpositionViewer(repo.getValue().id));
        this.adapter = new AutoClearedValue<>(this, adapter);

        binding.get().bottomSheetList.expositionList.setAdapter(adapter);
    }

    private void initStoryImageListener() {
        binding.get().createStoryImage.setOnClickListener(v -> {
            photoFile = FileUtilKt.dispatchTakePictureIntent(TAKE_STORY_PICTURE_REQUEST_CODE, this, photoFile);
        });
    }

    private void initGenreSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.genre_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.get().genreSpinner.setAdapter(adapter);
        binding.get().genreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                createViewModel.setGenre(binding.get().genreSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
            createViewModel.addChapter(chapterName, LocationUtilKt.locationToLatLng(mCurrentLocation), 10);

            // Add marker to map
            LatLng chapterLocation = LocationUtilKt.locationToLatLng(mCurrentLocation);

            Marker newMarker = mMap.addMarker(new MarkerOptions()
                    .position(chapterLocation)
                    .title(chapterName));
            markerArrayList.add(newMarker);

            // Get bounds of all markers
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markerArrayList) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            int padding = 0; // offset from edges of the map in pixels
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cameraUpdate);

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

            // Add text input to viewmodel
            createViewModel.setDescription(String.valueOf(binding.get().storyDescEditText.getText()));
            createViewModel.setStoryName(String.valueOf(binding.get().storyNameEditText.getText()));
            createViewModel.setTags(Arrays.asList(String.valueOf(binding.get().storyTags.getText()).split(" ")));

            createViewModel.finishStoryPart1(getContext()).observe(this, publishSuccessful -> {

                if (binding.get().finishStoryInputs.getVisibility() == View.GONE) {
                    binding.get().finishStoryInputs.setVisibility(View.VISIBLE);
                } else {
                    binding.get().finishStoryInputs.setVisibility(View.GONE);
                }

                if (publishSuccessful != null && publishSuccessful.status == Status.SUCCESS) {
                    createViewModel.finishStoryPart2(publishSuccessful.data).observe(this, voidResource -> {

                        if (voidResource != null && voidResource.status == Status.SUCCESS) {
                            Toast.makeText(getContext(), "Story published successfully!", Toast.LENGTH_SHORT).show();
                            getActivity().onBackPressed();
                        }
                    });
                }
            });
        });
    }

    private void initAddTextListener() {
        binding.get().addTextExpositionButton.setOnClickListener((v) -> {
            try {
                String textExposition = String.valueOf(binding.get().storyExpositionEditText.getText());
                if (textExposition.isEmpty()) {
                    Toast.makeText(getContext(), "Text exposition cant be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    createViewModel.addExposition(ExpositionType.TEXT, textExposition);
                    binding.get().storyExpositionEditText.setText("");
                }
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
                photoFile = FileUtilKt.dispatchTakePictureIntent(TAKE_EXPOSITION_PICTURE_REQUEST_CODE, this, photoFile);
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

    private void initExpositionList(CreateViewModel viewModel) {
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentCreateBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_create, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return dataBinding.getRoot();
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
        if (requestCode == TAKE_EXPOSITION_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            createViewModel.addExposition(ExpositionType.PICTURE, photoFile.getAbsolutePath());
        } else if (requestCode == RECORD_AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {
            createViewModel.addExposition(ExpositionType.AUDIO, data.getData().getPath());
        } else if (requestCode == TAKE_STORY_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            createViewModel.setStoryImage(photoFile.getAbsolutePath());
            binding.get().createStoryImage.setImageURI(Uri.fromFile(photoFile));
        }
        adapter.get().notifyItemChanged(createViewModel.getAllChapters().size() - 1);
    }


}
