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

package com.android.example.github.ui.storyreader;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.StoryPlayFragmentBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.ChapterAdapter;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.util.AutoClearedValue;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;

import javax.inject.Inject;

/**
 * The UI Controller for displaying a Github Repo's information with its chapters.
 */
public class StoryPlayFragment extends Fragment implements
        LifecycleRegistryOwner,
        Injectable,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback {

    private static final String REPO_OWNER_KEY = "repo_owner";

    private static final String REPO_NAME_KEY = "repo_name";

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<StoryPlayFragmentBinding> binding;
    AutoClearedValue<ChapterAdapter> adapter;
    private StoryPlayViewModel storyPlayViewModel;
    private GoogleMap mMap;


    public static StoryPlayFragment create(String owner, String name) {
        StoryPlayFragment repoFragment = new StoryPlayFragment();
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
        storyPlayViewModel = ViewModelProviders.of(this, viewModelFactory).get(StoryPlayViewModel.class);
        Bundle args = getArguments();
        if (args != null && args.containsKey(REPO_OWNER_KEY) &&
                args.containsKey(REPO_NAME_KEY)) {
            storyPlayViewModel.setId(args.getString(REPO_OWNER_KEY),
                    args.getString(REPO_NAME_KEY));
        } else {
            storyPlayViewModel.setId(null, null);
        }
        LiveData<Resource<Repo>> repo = storyPlayViewModel.getRepo();
        repo.observe(this, resource -> {
            binding.get().setRepo(resource == null ? null : resource.data);
            binding.get().setRepoResource(resource);
            binding.get().executePendingBindings();
        });

        ChapterAdapter adapter = new ChapterAdapter(dataBindingComponent, false,
                chapter -> navigationController.navigateToSearch());
        this.adapter = new AutoClearedValue<>(this, adapter);
        binding.get().chapterList.setAdapter(adapter);
        initViewExpositionsListener();
        initViewMapListener();
        initContributorList(storyPlayViewModel);
//        Toast.makeText(getContext(), "Story id = " + args.get("storyIdKey").toString(), Toast.LENGTH_SHORT).show();
        getActivity().setTitle("Play Story");
    }

    private void initViewExpositionsListener() {
        binding.get().viewExpositions.setOnClickListener((v) -> {
            //Todo: Show all expositions
        });
    }

    private void initViewMapListener() {
        binding.get().viewMap.setOnClickListener((v) -> {
            //Todo: Open map
        });
    }

    private void initContributorList(StoryPlayViewModel viewModel) {
        adapter.get().replace(viewModel.getChapters());
//        viewModel.getChapters().observe(this, listResource -> {
//            // we don't need any null checks here for the adapter since LiveData guarantees that
//            // it won't call us if fragment is stopped or not started.
//            if (listResource != null && listResource.data != null) {
//                adapter.get().replace(listResource.data);
//            } else {
//                //noinspection ConstantConditions
//                adapter.get().replace(Collections.emptyList());
//            }
//        });
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        StoryPlayFragmentBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.story_play_fragment, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
