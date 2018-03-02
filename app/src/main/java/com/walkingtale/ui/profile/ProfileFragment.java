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

package com.walkingtale.ui.profile;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.walkingtale.MainActivity;
import com.walkingtale.R;
import com.walkingtale.binding.FragmentDataBindingComponent;
import com.walkingtale.databinding.FragmentProfileBinding;
import com.walkingtale.di.Injectable;
import com.walkingtale.ui.common.FileUtilKt;
import com.walkingtale.ui.common.NavigationController;
import com.walkingtale.ui.common.StoryListAdapter;
import com.walkingtale.util.AutoClearedValue;

import java.io.File;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

/**
 * The UI Controller for displaying the overview for a story.
 */
public class ProfileFragment extends Fragment implements Injectable {

    private final String TAG = this.getClass().getSimpleName();
    private final int RC_TAKE_PROFILE_IMAGE = 1;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<FragmentProfileBinding> binding;
    AutoClearedValue<StoryListAdapter> playedStoriesAdapter;
    AutoClearedValue<StoryListAdapter> createdStoriesAdapter;
    File photoFile;
    private ProfileViewModel profileViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentProfileBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_profile, container, false, dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        profileViewModel = ViewModelProviders.of(this, viewModelFactory).get(ProfileViewModel.class);

        StoryListAdapter playedStoriesAdapt = new StoryListAdapter(
                dataBindingComponent,
                repo -> profileViewModel.setUserId(repo.username),
                storyToDelete -> {
                    profileViewModel.deleteStory(storyToDelete);
                    // TODO: 3/1/18 notify data set has changed
                },
                this);
        binding.get().playedList.setAdapter(playedStoriesAdapt);
        playedStoriesAdapter = new AutoClearedValue<>(this, playedStoriesAdapt);


        StoryListAdapter createdStoriesAdapt = new StoryListAdapter(
                dataBindingComponent,
                repo -> profileViewModel.setUserId(repo.username),
                storyToDelete -> {
                    profileViewModel.deleteStory(storyToDelete);
                    // TODO: 3/1/18 notify data set has changed
                },
                this);
        binding.get().createdList.setAdapter(createdStoriesAdapt);
        createdStoriesAdapter = new AutoClearedValue<>(this, createdStoriesAdapt);

        profileViewModel.setUserId(MainActivity.getCognitoId());
        profileViewModel.user.observe(this, userResource -> {
            if (userResource != null) {
                Log.i(TAG, "" + userResource.data);
                binding.get().setUser(userResource.data);
            }
        });

        initPlayedStories();
        initCreatedStories();
        initTabHost();
        profileImageListener();
    }

    private void initTabHost() {
        TabHost tabHost = binding.get().tabs;
        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("First Tab");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Second Tab");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("Third Tab");

        tab1.setIndicator("Played");
        tab1.setContent(binding.get().tab1.getId());

        tab2.setIndicator("Created");
        tab2.setContent(binding.get().tab2.getId());

        tab3.setIndicator("Following");
        tab3.setContent(binding.get().tab3.getId());

        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);

        initTabListener();
    }

    private void initTabListener() {
        binding.get().tabs.setOnTabChangedListener(s -> {
            switch (binding.get().tabs.getCurrentTab()) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
        });
    }

    private void initPlayedStories() {
        profileViewModel.playedStories.observe(this, result -> {
            if (result != null && result.data != null) {
                playedStoriesAdapter.get().replace(result.data);
            }
            binding.get().executePendingBindings();
        });
    }

    private void initCreatedStories() {
        profileViewModel.createdStories.observe(this, result -> {
            if (result != null && result.data != null) {
                createdStoriesAdapter.get().replace(result.data);
            }
            binding.get().executePendingBindings();
        });
    }

    private void profileImageListener() {
        binding.get().avatar.setOnClickListener(v ->
                new AlertDialog.Builder(getContext())
                        .setTitle("Set profile image")
                        .setMessage("Do you want to change your profile image?")
                        .setPositiveButton("yes", (dialogInterface, i) ->
                                FileUtilKt.dispatchTakePictureIntent(RC_TAKE_PROFILE_IMAGE, this, photoFile))
                        .setNegativeButton("no", (dialogInterface, i) -> {
                        })
                        .create()
                        .show());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_TAKE_PROFILE_IMAGE && resultCode == RESULT_OK) {
            // TODO: 2/23/18 save file to s3, then update user in ddb
        }
    }
}
