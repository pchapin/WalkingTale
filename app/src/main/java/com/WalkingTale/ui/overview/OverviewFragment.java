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

package com.WalkingTale.ui.overview;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.WalkingTale.MainActivity;
import com.WalkingTale.R;
import com.WalkingTale.binding.FragmentDataBindingComponent;
import com.WalkingTale.databinding.FragmentOverviewBinding;
import com.WalkingTale.di.Injectable;
import com.WalkingTale.repository.tasks.StoryKey;
import com.WalkingTale.ui.common.NavigationController;
import com.WalkingTale.util.AutoClearedValue;
import com.WalkingTale.vo.Story;

import javax.inject.Inject;

/**
 * The UI Controller for displaying the overview for a story.
 */
public class OverviewFragment extends Fragment implements Injectable {

    private static final String TAG = OverviewFragment.class.getSimpleName();
    private static final String REPO_NAME_KEY = "repo_name";
    private static final String REPO_USER_KEY = "repo_user";
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<FragmentOverviewBinding> binding;
    private OverviewViewModel overviewViewModel;
    private Story story;

    public static OverviewFragment create(Story s) {
        OverviewFragment repoFragment = new OverviewFragment();
        Bundle args = new Bundle();
        args.putString(REPO_NAME_KEY, s.id);
        args.putString(REPO_USER_KEY, s.userId);
        repoFragment.setArguments(args);
        return repoFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        overviewViewModel = ViewModelProviders.of(this, viewModelFactory).get(OverviewViewModel.class);
        Bundle args = getArguments();
        if (args != null && args.containsKey(REPO_NAME_KEY) && args.containsKey(REPO_USER_KEY)) {
            overviewViewModel.getStory(new StoryKey(args.getString(REPO_USER_KEY), args.getString(REPO_NAME_KEY))).observe(this, resource -> {
                Log.i(TAG, "" + resource);
                if (resource != null && resource.data != null) {
                    binding.get().setStory(resource.data);
                    story = resource.data;
                    binding.get().setRepoResource(resource);
                    binding.get().executePendingBindings();
                    binding.get().toolbar.setTitle(story.storyName);

                    if (MainActivity.DEBUG_MODE == MainActivity.DEBUG_STATE.PLAY) {
                        navigationController.navigateToStoryPlay(resource.data);
                    }
                }
            });
        }

        initStoryLocationListener();
        fabListener();
    }

    private void fabListener() {
        binding.get().overviewFab.setOnClickListener(v -> {
            navigationController.navigateToStoryPlay(story);
        });
    }

    private void initStoryLocationListener() {
        binding.get().storyLocationButton.setOnClickListener((v) -> {
            String latitude = Double.toString(story.chapters.get(0).getLocation().latitude);
            String longitude = Double.toString(story.chapters.get(0).getLocation().longitude);
            String url = String.format("https://www.google.com/maps/search/?api=1&query=%s,%s", latitude, longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentOverviewBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_overview, container, false, dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }
}
