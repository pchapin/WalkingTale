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

package com.android.example.github.ui.feed;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.example.github.MainActivity;
import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.FragmentFeedBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.ui.common.PermissionManager;
import com.android.example.github.ui.common.StoryListAdapter;
import com.android.example.github.util.AutoClearedValue;

import javax.inject.Inject;

/**
 * The UI controller for the main screen of the app, the feed.
 */
public class FeedFragment extends Fragment implements Injectable {

    private final String TAG = this.getClass().getSimpleName();
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<FragmentFeedBinding> binding;
    AutoClearedValue<StoryListAdapter> adapter;
    private FeedViewModel feedViewModel;
    private Toolbar toolbar;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentFeedBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_feed, container, false,
                        dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        feedViewModel = ViewModelProviders.of(this, viewModelFactory).get(FeedViewModel.class);
        toolbar = binding.get().toolbar;
        fab = binding.get().feedFloatingActionButton;
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setSupportActionBar(toolbar);
        menuClickListener();
        initRecyclerView();
        fabListener();

        StoryListAdapter rvAdapter = new StoryListAdapter(dataBindingComponent, story -> {
            if (PermissionManager.checkLocationPermission(getActivity())) {
                navigationController.navigateToOverview(story);
            }
        });

        binding.get().repoList.setAdapter(rvAdapter);
        adapter = new AutoClearedValue<>(this, rvAdapter);
        binding.get().setCallback(() -> feedViewModel.getResults());
    }

    private void menuClickListener() {
        toolbar.setTitleTextColor(getActivity().getColor(R.color.white));
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_profile:
                    navigationController.navigateToProfile();
                    break;
                case R.id.action_search:
                    navigationController.navigateToSearch();
                    break;
            }
            return true;
        });
    }

    private void initRecyclerView() {
        feedViewModel.getResults().observe(this, result -> {
            Log.i(TAG, "" + result);
            if (result != null) Log.i(TAG, "" + result.data);
            binding.get().setSearchResource(result);
            binding.get().setResultCount((result == null || result.data == null)
                    ? 0 : result.data.size());
            adapter.get().replace(result == null ? null : result.data);
            binding.get().executePendingBindings();

            if (MainActivity.DEBUG_MODE) {
                if (result != null && result.data != null && !result.data.isEmpty()) {
                    navigationController.navigateToOverview(result.data.get(0));
                }
            }
        });
    }

    private void fabListener() {
        fab.setOnClickListener(v -> navigationController.navigateToCreateStory());
    }
}
