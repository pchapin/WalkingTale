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

package com.android.example.github.ui.expositionviewer;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.ExpositionViewerFragmentBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.ExpositionAdapter;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.util.AutoClearedValue;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;
import com.android.example.github.walkingTale.Chapter;
import com.android.example.github.walkingTale.Exposition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * The UI Controller for displaying a list of expositions.
 */
public class ExpositionViewerFragment extends Fragment implements LifecycleRegistryOwner, Injectable {

    private static final String REPO_OWNER_KEY = "repo_owner";

    private static final String REPO_NAME_KEY = "repo_name";

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<ExpositionViewerFragmentBinding> binding;
    AutoClearedValue<ExpositionAdapter> adapter;
    Gson gson = new Gson();
    private ExpositionViewerViewModel expositionViewerViewModel;

    public static ExpositionViewerFragment create(String owner, String name) {
        ExpositionViewerFragment expositionViewerFragment = new ExpositionViewerFragment();
        Bundle args = new Bundle();
        args.putString(REPO_OWNER_KEY, owner);
        args.putString(REPO_NAME_KEY, name);
        expositionViewerFragment.setArguments(args);
        return expositionViewerFragment;
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        expositionViewerViewModel = ViewModelProviders.of(this, viewModelFactory).get(ExpositionViewerViewModel.class);
        Bundle args = getArguments();
        if (args != null && args.containsKey(REPO_OWNER_KEY) &&
                args.containsKey(REPO_NAME_KEY)) {
            expositionViewerViewModel.setId(args.getString(REPO_OWNER_KEY),
                    args.getString(REPO_NAME_KEY));
        } else {
            expositionViewerViewModel.setId(null, null);
        }
        LiveData<Resource<Repo>> repo = expositionViewerViewModel.getRepo();
        repo.observe(this, resource -> {
            binding.get().setRepo(resource == null ? null : resource.data);
            binding.get().executePendingBindings();
        });

        ExpositionAdapter adapter = new ExpositionAdapter(dataBindingComponent, false,
                chapter -> {
                    // TODO: 10/30/2017 Do something if user clicks an exposition, animation maybe?
                });
        this.adapter = new AutoClearedValue<>(this, adapter);
        binding.get().expositionList.setAdapter(adapter);

        initContributorList(expositionViewerViewModel);
        getActivity().setTitle("Exposition Viewer");
    }


    private void initContributorList(ExpositionViewerViewModel viewModel) {
        viewModel.getRepo().observe(this, listResource -> {
            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (listResource != null && listResource.data != null) {
                Type listType = new TypeToken<List<Chapter>>() {
                }.getType();

                List<Chapter> chapters = gson.fromJson(listResource.data.chapters, listType);
                List<Exposition> expositions = new ArrayList<>();
                for (Chapter chapter : chapters) {
                    expositions.addAll(chapter.getExpositions());
                }
                adapter.get().replace(expositions);
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
        ExpositionViewerFragmentBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.exposition_viewer_fragment, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }
}
