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

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.example.github.R;
import com.android.example.github.databinding.ItemStoryBinding;
import com.android.example.github.util.Objects;
import com.android.example.github.vo.Story;

/**
 * A RecyclerView adapter for {@link Story} class.
 */
public class StoryListAdapter extends DataBoundListAdapter<Story, ItemStoryBinding> {
    private final DataBindingComponent dataBindingComponent;
    private final StoryClickCallback storyClickCallback;

    public StoryListAdapter(DataBindingComponent dataBindingComponent, StoryClickCallback storyClickCallback) {
        this.dataBindingComponent = dataBindingComponent;
        this.storyClickCallback = storyClickCallback;
    }

    @Override
    protected ItemStoryBinding createBinding(ViewGroup parent) {
        ItemStoryBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.item_story,
                        parent, false, dataBindingComponent);
        binding.getRoot().setOnClickListener(v -> {
            Story story = binding.getStory();
            if (story != null && storyClickCallback != null) {
                storyClickCallback.onClick(story);
            }
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
}
