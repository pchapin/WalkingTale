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

package com.MapPost.ui.common;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.MapPost.R;
import com.MapPost.databinding.ItemStoryBinding;
import com.MapPost.ui.feed.FeedFragment;
import com.MapPost.ui.profile.ProfileFragment;
import com.MapPost.util.Objects;
import com.MapPost.vo.Story;

/**
 * A RecyclerView adapter for {@link Story} class.
 */
public class StoryListAdapter extends DataBoundListAdapter<Story, ItemStoryBinding> {
    private final DataBindingComponent dataBindingComponent;
    private final StoryClickCallback storyClickCallback;
    private final ReportStoryCallback reportStoryCallback;
    private final ShareStoryCallback shareStoryCallback;
    private final SaveStoryCallback saveStoryCallback;
    private final DeleteStoryCallback deleteStoryCallback;
    private final Object menuHost;

    public StoryListAdapter(DataBindingComponent dataBindingComponent, StoryClickCallback storyClickCallback, DeleteStoryCallback deleteStoryCallback, ProfileFragment profileFragment) {
        this.dataBindingComponent = dataBindingComponent;
        this.storyClickCallback = storyClickCallback;
        this.reportStoryCallback = null;
        this.shareStoryCallback = null;
        this.saveStoryCallback = null;
        this.deleteStoryCallback = deleteStoryCallback;
        this.menuHost = profileFragment;
    }

    public StoryListAdapter(
            DataBindingComponent dataBindingComponent,
            StoryClickCallback storyClickCallback,
            ReportStoryCallback reportStoryCallback,
            SaveStoryCallback saveStoryCallback,
            ShareStoryCallback shareStoryCallback,
            FeedFragment feedFragment) {
        this.dataBindingComponent = dataBindingComponent;
        this.storyClickCallback = storyClickCallback;
        this.reportStoryCallback = reportStoryCallback;
        this.saveStoryCallback = saveStoryCallback;
        this.shareStoryCallback = shareStoryCallback;
        this.deleteStoryCallback = null;
        this.menuHost = feedFragment;
    }

    @Override
    protected ItemStoryBinding createBinding(ViewGroup parent) {
        ItemStoryBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_story,
                parent, false, dataBindingComponent);
        binding.getRoot().setOnClickListener(v -> {
            Story story = binding.getStory();
            if (story != null && storyClickCallback != null) {
                storyClickCallback.onClick(story);
            }
        });

        binding.storyMenuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(parent.getContext(), v);

            if (menuHost instanceof FeedFragment) {
                popupMenu.getMenuInflater().inflate(R.menu.story_feed_menu, popupMenu.getMenu());
            } else if (menuHost instanceof ProfileFragment) {
                popupMenu.getMenuInflater().inflate(R.menu.story_profile_menu, popupMenu.getMenu());
            }

            // Set a listener so we are notified if a menu item is clicked
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                Story story = binding.getStory();
                switch (menuItem.getItemId()) {
                    case R.id.menu_report_story:
                        reportStoryCallback.onClick(story);
                        return true;
                    case R.id.menu_save_story:
                        saveStoryCallback.onClick(story);
                        return true;
                    case R.id.menu_share_story:
                        shareStoryCallback.onClick(story);
                        return true;
                    case R.id.menu_delete_story:
                        deleteStoryCallback.onClick(story);
                }
                return false;
            });
            popupMenu.show();
        });

        return binding;
    }

    @Override
    protected void bind(ItemStoryBinding binding, Story item) {
        binding.setStory(item);
    }

    @Override
    protected boolean areItemsTheSame(Story oldItem, Story newItem) {
        return Objects.equals(oldItem, newItem);
    }

    @Override
    protected boolean areContentsTheSame(Story oldItem, Story newItem) {
        return Objects.equals(oldItem.chapters, newItem.chapters);
    }

    public interface StoryClickCallback {
        void onClick(Story story);
    }

    public interface ReportStoryCallback {
        void onClick(Story story);
    }

    public interface SaveStoryCallback {
        void onClick(Story story);
    }

    public interface ShareStoryCallback {
        void onClick(Story story);
    }

    public interface DeleteStoryCallback {
        void onClick(Story story);
    }
}
