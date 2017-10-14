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

package com.android.example.github.ui.storycreate;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.CreateStoryFragmentBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.ui.repo.ContributorAdapter;
import com.android.example.github.util.AutoClearedValue;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Collections;

import javax.inject.Inject;

/**
 * The UI Controller for displaying a Github Repo's information with its contributors.
 */
public class StoryCreateFragment extends Fragment implements LifecycleRegistryOwner, Injectable {

    private static final String REPO_OWNER_KEY = "repo_owner";

    private static final String REPO_NAME_KEY = "repo_name";

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<CreateStoryFragmentBinding> binding;
    AutoClearedValue<ContributorAdapter> adapter;
    private StoryCreateViewModel StoryCreateViewModel;

    public static StoryCreateFragment create(String owner, String name) {
        StoryCreateFragment repoFragment = new StoryCreateFragment();
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
        StoryCreateViewModel = ViewModelProviders.of(this, viewModelFactory).get(StoryCreateViewModel.class);
        Bundle args = getArguments();
        if (args != null && args.containsKey(REPO_OWNER_KEY) &&
                args.containsKey(REPO_NAME_KEY)) {
            StoryCreateViewModel.setId(args.getString(REPO_OWNER_KEY),
                    args.getString(REPO_NAME_KEY));
        } else {
            StoryCreateViewModel.setId(null, null);
        }
        LiveData<Resource<Repo>> repo = StoryCreateViewModel.getRepo();
        repo.observe(this, resource -> {
            binding.get().setRepo(resource == null ? null : resource.data);
            binding.get().setRepoResource(resource);
            binding.get().executePendingBindings();
        });

        ContributorAdapter adapter = new ContributorAdapter(dataBindingComponent,
                contributor -> navigationController.navigateToUser(contributor.getLogin()));
        this.adapter = new AutoClearedValue<>(this, adapter);
        binding.get().contributorList.setAdapter(adapter);
        initAddChapterListener();
        initFinishStoryListener();
        initContributorList(StoryCreateViewModel);
    }

    private void initAddChapterListener() {
        binding.get().addChapterButton.setOnClickListener((v) -> {
            navigationController.navigateToChapterCreate();
        });
    }

    private void initFinishStoryListener() {
        binding.get().finishStory.setOnClickListener((v) -> {
            //Todo: If story info is valid, publish story
        });
    }

    private void initContributorList(StoryCreateViewModel viewModel) {
        viewModel.getContributors().observe(this, listResource -> {
            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (listResource != null && listResource.data != null) {
                adapter.get().replace(listResource.data);
            } else {
                //noinspection ConstantConditions
                adapter.get().replace(Collections.emptyList());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        CreateStoryFragmentBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.create_story_fragment, container, false);
        dataBinding.setRetryCallback(() -> StoryCreateViewModel.retry());
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }
}
