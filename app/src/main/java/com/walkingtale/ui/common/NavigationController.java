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

package com.walkingtale.ui.common;

import android.support.v4.app.FragmentManager;

import com.walkingtale.MainActivity;
import com.walkingtale.R;
import com.walkingtale.ui.create.CreateFragment;
import com.walkingtale.ui.feed.FeedFragment;
import com.walkingtale.ui.overview.OverviewFragment;
import com.walkingtale.ui.play.PlayFragment;
import com.walkingtale.ui.profile.ProfileFragment;
import com.walkingtale.ui.search.SearchFragment;
import com.walkingtale.vo.Story;

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

    public void navigateToOverview(Story s) {
        OverviewFragment fragment = OverviewFragment.create(s);
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

    public void navigateToStoryPlay(Story story) {
        PlayFragment storyPlayFragment = PlayFragment.create(story);
        fragmentManager.beginTransaction()
                .replace(containerId, storyPlayFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void navigateToSearch() {
        SearchFragment searchFragment = new SearchFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, searchFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void navigateToProfile() {
        ProfileFragment profileFragment = new ProfileFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, profileFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }
}
