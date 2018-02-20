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

package com.android.example.github.ui.album;

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

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.FragmentAlbumBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.ExpositionAdapter;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.util.AutoClearedValue;
import com.android.example.github.vo.Resource;
import com.android.example.github.vo.Story;
import com.android.example.github.walkingTale.Chapter;
import com.android.example.github.walkingTale.Exposition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * The UI Controller for displaying a list of expositions.
 */
public class AlbumFragment extends Fragment implements Injectable {

    private static final String REPO_NAME_KEY = "repo_name";
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<FragmentAlbumBinding> binding;
    AutoClearedValue<ExpositionAdapter> adapter;
    private AlbumViewModel albumViewModel;
    public static AlbumFragment create(String id) {
        AlbumFragment expositionViewerFragment = new AlbumFragment();
        Bundle args = new Bundle();
        args.putString(REPO_NAME_KEY, id);
        expositionViewerFragment.setArguments(args);
        return expositionViewerFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        albumViewModel = ViewModelProviders.of(this, viewModelFactory).get(AlbumViewModel.class);
        Bundle args = getArguments();
        if (args != null && args.containsKey(REPO_NAME_KEY)) {
            albumViewModel.setId(args.getString(REPO_NAME_KEY));
        } else {
            albumViewModel.setId(null);
        }
        LiveData<Resource<Story>> repo = albumViewModel.getRepo();
        repo.observe(this, resource -> {
            binding.get().setStory(resource == null ? null : resource.data);
            binding.get().executePendingBindings();
        });

        ExpositionAdapter adapter = new ExpositionAdapter(dataBindingComponent,
                chapter -> {
                    // TODO: 10/30/2017 Do something if user clicks an exposition, animation maybe?

                });
        this.adapter = new AutoClearedValue<>(this, adapter);
        binding.get().expositionList.setAdapter(adapter);

        initContributorList(albumViewModel);
        getActivity().setTitle("Exposition Viewer");
    }


    private void initContributorList(AlbumViewModel viewModel) {
        viewModel.getRepo().observe(this, listResource -> {
            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (listResource != null && listResource.data != null && listResource.data.chapters != null) {

                List<Exposition> expositions = new ArrayList<>();
                for (Chapter chapter : listResource.data.chapters) {
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
        FragmentAlbumBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_album, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }
}
