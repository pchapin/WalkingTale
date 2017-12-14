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

package com.android.example.github.ui.common;

import com.android.example.github.MainActivity;
import com.android.example.github.R;
import com.android.example.github.ui.album.AlbumFragment;
import com.android.example.github.ui.create.CreateFragment;
import com.android.example.github.ui.feed.FeedFragment;
import com.android.example.github.ui.overview.OverviewFragment;
import com.android.example.github.ui.play.PlayFragment;

import android.support.v4.app.FragmentManager;

import javax.inject.Inject;

/**
 * A utility class that handles navigation in {@link MainActivity}.
 */
public class NavigationController {
    private final int containerId;
    private final FragmentManager fragmentManager;

    @Inject
    public NavigationController(MainActivity mainActivity) {
        this.containerId = R.id.container;
        this.fragmentManager = mainActivity.getSupportFragmentManager();
    }

    public void navigateToRepo(String id) {
        OverviewFragment fragment = OverviewFragment.create(id);
        fragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void navigateToCreateStory() {
        CreateFragment storyCreateFragment = new CreateFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, storyCreateFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void navigateToStoryFeed() {
        FeedFragment storyFeedFragment = new FeedFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, storyFeedFragment)
                .commitAllowingStateLoss();
    }

    public void navigateToStoryPlay(String id) {
        PlayFragment storyPlayFragment = PlayFragment.create(id);
        fragmentManager.beginTransaction()
                .replace(containerId, storyPlayFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void navigateToExpositionViewer(String id) {
        AlbumFragment expositionViewerFragment = AlbumFragment.create(id);
        fragmentManager.beginTransaction()
                .replace(containerId, expositionViewerFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }
}
