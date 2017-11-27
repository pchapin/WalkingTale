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

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.CreateFragmentBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.audiorecord.AudioRecordActivity;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.util.AutoClearedValue;
import com.android.example.github.walkingTale.Chapter;
import com.android.example.github.walkingTale.ExpositionType;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import android.Manifest;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * The UI Controller for creating a story.
 */
public class CreateFragment extends Fragment implements
        LifecycleRegistryOwner,
        Injectable,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback {

    // Constants
    private static final String REPO_OWNER_KEY = "repo_owner";
    private static final String REPO_NAME_KEY = "repo_name";
    public static String AUDIO_KEY_CHAPTER = "AUDIO_KEY_CHAPTER";
    public static String AUDIO_KEY_EXPOSITION = "AUDIO_KEY_EXPOSITION";
    private final int RECORD_AUDIO_REQUEST_CODE = 123;
    private final int TAKE_PICTURE_REQUEST_CODE = 1234;
    private final int UPDATE_LOCATION_REQUEST_CODE = 12345;
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<CreateFragmentBinding> binding;
    private GoogleMap mMap;
    private LatLng mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private CreateViewModel createViewModel;
    private ArrayList<Marker> markerArrayList = new ArrayList<>();

    public static CreateFragment create(String owner, String name) {
        CreateFragment repoFragment = new CreateFragment();
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
        createViewModel = ViewModelProviders.of(this, viewModelFactory).get(CreateViewModel.class);
        Bundle args = getArguments();
        initAddChapterListener();
        initRemoveChapterListener();
        initFinishStoryListener();
        initAddTextListener();
        initAddPictureListener();
        initAddAudioListener();
        initRadiusIncrementListener();
        initRadiusDecrementListener();
        getActivity().setTitle("Create Story");


        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void updateChapterList() {
        LinearLayout linearLayout = binding.get().chapterListLinearLayout;
        linearLayout.removeAllViews();

        for (Chapter chapter : createViewModel.storyManager.getAllChapters()) {
            TextView textView = new TextView(getContext());
            textView.setText(chapter.toString());
            linearLayout.addView(textView);
        }
    }

    private void initAddChapterListener() {
        binding.get().addChapterButton.setOnClickListener((v) -> {
            if (mLastLocation != null) {
                // TODO: 10/27/2017 get chapter name from the author
                String chapterName = "Chapter Name";
                createViewModel.storyManager.addChapter(chapterName, mLastLocation, 1);

                // Add marker to map
                LatLng chapterLocation = new LatLng(mLastLocation.latitude, mLastLocation.longitude);

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

            } else {
                // TODO: 10/23/2017 Location is required, deal with lack of location updates
                Toast.makeText(getContext(), "Location is null.", Toast.LENGTH_SHORT).show();
                createViewModel.storyManager.addChapter("Chapter name", new LatLng(1.1, 2.2), 1);
            }
            updateChapterList();
        });
    }

    private void initRemoveChapterListener() {
        binding.get().removeChapterButton.setOnClickListener((v) -> {
            try {
                createViewModel.storyManager.removeChapter();
                // Remove marker from map and list
                markerArrayList.get(markerArrayList.size() - 1).remove();
                markerArrayList.remove(markerArrayList.size() - 1);
            } catch (ArrayIndexOutOfBoundsException e) {
                Toast.makeText(getContext(), "No chapters to remove.", Toast.LENGTH_SHORT).show();
            }
            updateChapterList();
        });
    }

    private void initFinishStoryListener() {
        binding.get().finishStoryButton.setOnClickListener((v) -> {
            if (createViewModel.storyManager.getAllChapters().size() < 2) {
                Toast.makeText(getContext(), "Your story must have at least 2 chapters.", Toast.LENGTH_SHORT).show();
            } else {
                createViewModel.finishStory(getContext());
            }
        });
    }

    private void initAddTextListener() {
        binding.get().addTextExpositionButton.setOnClickListener((v) -> {
            try {
                createViewModel.storyManager.addExposition(ExpositionType.TEXT, "hello world");
            } catch (NoSuchElementException e) {
                Toast.makeText(getContext(), "No chapters to add expositions to.", Toast.LENGTH_SHORT).show();
            }
            updateChapterList();
        });
    }

    private void initAddPictureListener() {
        binding.get().addPictureExpositionButton.setOnClickListener((v) -> {
            if (createViewModel.storyManager.getAllChapters().isEmpty()) {
                Toast.makeText(getContext(), "No chapters to add expositions to.", Toast.LENGTH_SHORT).show();
            } else {
                dispatchTakePictureIntent();
            }
        });
    }

    private void initAddAudioListener() {
        binding.get().addAudioExpositionButton.setOnClickListener((v) -> {
            if (createViewModel.storyManager.getAllChapters().isEmpty()) {
                Toast.makeText(getContext(), "No chapters to add expositions to.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getActivity(), AudioRecordActivity.class);
                intent.setType("audio/mpeg4-generic");
                Chapter latestChapter = createViewModel.storyManager.getLatestChapter();
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
                createViewModel.storyManager.incrementRadius();
            } catch (NoSuchElementException e) {
                Toast.makeText(getContext(), "No chapters to increment radius.", Toast.LENGTH_SHORT).show();
            } catch (ArrayIndexOutOfBoundsException e) {
                Toast.makeText(getContext(), "Radius is already at max size.", Toast.LENGTH_SHORT).show();
            }
            updateChapterList();
        });
    }

    private void initRadiusDecrementListener() {
        binding.get().radiusDecrementButton.setOnClickListener((v) -> {
            try {
                createViewModel.storyManager.decrementRadius();
            } catch (NoSuchElementException e) {
                Toast.makeText(getContext(), "No chapters to decrement radius.", Toast.LENGTH_SHORT).show();
            } catch (ArrayIndexOutOfBoundsException e) {
                Toast.makeText(getContext(), "Radius is already at min size.", Toast.LENGTH_SHORT).show();
            }
            updateChapterList();
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        CreateFragmentBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.create_fragment, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);


        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        return dataBinding.getRoot();
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_PICTURE_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            createViewModel.storyManager.addExposition(ExpositionType.PICTURE, imageBitmap.toString());
            updateChapterList();
        } else if (requestCode == RECORD_AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri audioUri = data.getData();
            createViewModel.storyManager.addExposition(ExpositionType.AUDIO, audioUri.toString());
            updateChapterList();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        updateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void updateLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, UPDATE_LOCATION_REQUEST_CODE);
        } else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                mLastLocation = new LatLng(0.0, 0.0);
            } else {
                mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case UPDATE_LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation();
                } else {
                    Toast.makeText(getContext(), "This app needs location permissions.", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                }
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
}
