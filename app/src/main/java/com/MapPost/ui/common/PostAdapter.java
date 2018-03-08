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

import com.MapPost.R;
import com.MapPost.databinding.ItemExpositionBinding;
import com.MapPost.util.Objects;
import com.MapPost.vo.Post;

/**
 * A RecyclerView adapter for {@link Post} class.
 */
public class PostAdapter extends DataBoundListAdapter<Post, ItemExpositionBinding> {
    private final DataBindingComponent dataBindingComponent;
    private final ExpositionClickBack expositionClickBack;

    public PostAdapter(DataBindingComponent dataBindingComponent, ExpositionClickBack expositionClickBack) {
        this.dataBindingComponent = dataBindingComponent;
        this.expositionClickBack = expositionClickBack;
    }

    @Override
    protected ItemExpositionBinding createBinding(ViewGroup parent) {
        ItemExpositionBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_exposition,
                parent,
                false,
                dataBindingComponent);
        binding.getRoot().setOnClickListener(v -> {
            Post post = binding.getPost();
            if (post != null && expositionClickBack != null) {
                expositionClickBack.onClick(post);
            }
        });
        return binding;
    }

    @Override
    protected void bind(ItemExpositionBinding binding, Post item) {
        binding.setPost(item);
    }

    @Override
    protected boolean areItemsTheSame(Post oldItem, Post newItem) {
        return Objects.equals(oldItem.getPostId(), newItem.getPostId());
    }

    @Override
    protected boolean areContentsTheSame(Post oldItem, Post newItem) {
        return Objects.equals(oldItem.getPostId(), newItem.getPostId());
    }

    public interface ExpositionClickBack {
        void onClick(Post post);
    }
}
