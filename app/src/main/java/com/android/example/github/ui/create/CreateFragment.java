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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.FragmentCreateBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.audiorecord.AudioRecordActivity;
import com.android.example.github.ui.common.ChapterAdapter;
import com.android.example.github.ui.common.CreateFileKt;
import com.android.example.github.ui.common.LocationLiveData;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.util.AutoClearedValue;
import com.android.example.github.vo.Status;
import com.android.example.github.vo.Story;
import com.android.example.github.walkingTale.Chapter;
import com.android.example.github.walkingTale.ExpositionType;
import com.android.example.github.walkingTale.LocationUtilKt;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
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

    // This file is used to hold images temporarily
    private File photoFile = null;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        createViewModel = ViewModelProviders.of(this, viewModelFactory).get(CreateViewModel.class);
        initStoryNameListener();
        initDescriptionListener();
        initTagsListener();
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
        initContributorList(createViewModel);
        getActivity().setTitle("Create Story");

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
            Log.i("location", "" + mCurrentLocation + new Date().toString());
        });

        ChapterAdapter adapter = new ChapterAdapter(dataBindingComponent,
                chapter -> navigationController.navigateToExpositionViewer(repo.getValue().id));
        this.adapter = new AutoClearedValue<>(this, adapter);

        binding.get().chapterList.setAdapter(adapter);
    }

    private void initStoryNameListener() {
        binding.get().storyNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                createViewModel.setStoryName(editable.toString());
            }
        });
    }

    private void initDescriptionListener() {
        binding.get().storyDescEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                createViewModel.setDescription(editable.toString());
            }
        });
    }

    private void initTagsListener() {
        binding.get().storyTags.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                createViewModel.setTags(Collections.singletonList("tags"));
            }
        });
    }

    private void initStoryImageListener() {
        binding.get().createStoryImage.setOnClickListener(v -> {
            dispatchTakePictureIntent(TAKE_STORY_PICTURE_REQUEST_CODE);
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
            createViewModel.addChapter(chapterName, LocationUtilKt.LocationToLatLng(mCurrentLocation), 10);

            // Add marker to map
            LatLng chapterLocation = LocationUtilKt.LocationToLatLng(mCurrentLocation);

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
            createViewModel.finishStoryPart1(getContext()).observe(this, publishSuccessful -> {

                if (publishSuccessful != null && publishSuccessful.status == Status.SUCCESS) {

                    Log.i("returned chapters", "" + publishSuccessful.data.chapters);

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
                dispatchTakePictureIntent(TAKE_EXPOSITION_PICTURE_REQUEST_CODE);
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
        FragmentCreateBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_create, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return dataBinding.getRoot();
    }

    private void dispatchTakePictureIntent(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = CreateFileKt.createFile(getActivity());
            } catch (IOException ignored) {
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.android.example.github",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, requestCode);
            }
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
        if (requestCode == TAKE_EXPOSITION_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            createViewModel.addExposition(ExpositionType.PICTURE, photoFile.getAbsolutePath());
        } else if (requestCode == RECORD_AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {
            createViewModel.addExposition(ExpositionType.AUDIO, data.getData().toString());
        } else if (requestCode == TAKE_STORY_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            createViewModel.setStoryImage(photoFile.getAbsolutePath());
            binding.get().createStoryImage.setImageURI(Uri.fromFile(photoFile));
        }
        adapter.get().notifyItemChanged(createViewModel.getAllChapters().size() - 1);
    }
}
