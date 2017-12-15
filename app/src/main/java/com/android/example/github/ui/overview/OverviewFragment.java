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

package com.android.example.github.ui.overview;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.OverviewFragmentBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.ui.common.PermissionManager;
import com.android.example.github.util.AutoClearedValue;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

/**
 * The UI Controller for displaying the overview for a story.
 */
public class OverviewFragment extends LifecycleFragment implements LifecycleRegistryOwner, Injectable {

    private static final String REPO_NAME_KEY = "repo_name";

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<OverviewFragmentBinding> binding;
    private OverviewViewModel overviewViewModel;

    public static OverviewFragment create(String id) {
        OverviewFragment repoFragment = new OverviewFragment();
        Bundle args = new Bundle();
        args.putString(REPO_NAME_KEY, id);
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
        overviewViewModel = ViewModelProviders.of(this, viewModelFactory).get(OverviewViewModel.class);
        Bundle args = getArguments();
        if (args != null && args.containsKey(REPO_NAME_KEY)) {
            overviewViewModel.setId(args.getString(REPO_NAME_KEY));
        } else {
            overviewViewModel.setId(null);
        }
        LiveData<Resource<Repo>> repo = overviewViewModel.getRepo();
        repo.observe(this, resource -> {
            binding.get().setRepo(resource == null ? null : resource.data);
            binding.get().setRepoResource(resource);
            binding.get().executePendingBindings();
        });

        initStartStoryListener();
        initStoryLocationListener();
        initNavigationBarListener();
        getActivity().setTitle("Story Overview");
    }

    private void initStartStoryListener() {
        binding.get().startStoryButton.setOnClickListener((v) -> {
            String name = overviewViewModel.getRepo().getValue().data.id;
            navigationController.navigateToStoryPlay(name);
        });
    }

    private void initStoryLocationListener() {
        binding.get().storyLocationButton.setOnClickListener((v) -> {
            String latitude = Double.toString(overviewViewModel.getRepo().getValue().data.latitude);
            String longitude = Double.toString(overviewViewModel.getRepo().getValue().data.longitude);
            String url = String.format("https://www.google.com/maps/search/?api=1&query=%s,%s", latitude, longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        OverviewFragmentBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.overview_fragment, container, false, dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    private void initNavigationBarListener() {
        binding.get().bottomNavigation.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.action_home:
                            navigationController.navigateToStoryFeed();
                            break;
                        case R.id.action_search:
                            break;
                        case R.id.action_create:
                            if (PermissionManager.checkLocationPermission(getActivity())) {
                                navigationController.navigateToCreateStory();
                            }
                            break;
                        case R.id.action_play:
                            // TODO: 12/15/17 Should there be a dedicated play fragment to show stories in progress?
                            break;
                        case R.id.action_profile:
                            navigationController.navigateToUserProfile(getContext());
                            break;
                    }
                    return true;
                });
    }
}
