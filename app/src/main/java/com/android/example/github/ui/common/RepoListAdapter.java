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
import com.android.example.github.databinding.ItemRepoBinding;
import com.android.example.github.util.Objects;
import com.android.example.github.vo.Repo;

/**
 * A RecyclerView adapter for {@link Repo} class.
 */
public class RepoListAdapter extends DataBoundListAdapter<Repo, ItemRepoBinding> {
    private final DataBindingComponent dataBindingComponent;
    private final RepoClickCallback repoClickCallback;
    private final boolean showFullName;

    public RepoListAdapter(DataBindingComponent dataBindingComponent, boolean showFullName,
                           RepoClickCallback repoClickCallback) {
        this.dataBindingComponent = dataBindingComponent;
        this.repoClickCallback = repoClickCallback;
        this.showFullName = showFullName;
    }

    @Override
    protected ItemRepoBinding createBinding(ViewGroup parent) {
        ItemRepoBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.item_repo,
                        parent, false, dataBindingComponent);
        binding.setShowFullName(showFullName);
        binding.getRoot().setOnClickListener(v -> {
            Repo repo = binding.getRepo();
            if (repo != null && repoClickCallback != null) {
                repoClickCallback.onClick(repo);
            }
        });
        return binding;
    }

    @Override
    protected void bind(ItemRepoBinding binding, Repo item) {
        binding.setRepo(item);
    }

    @Override
    protected boolean areItemsTheSame(Repo oldItem, Repo newItem) {
        return Objects.equals(oldItem, newItem);
    }

    @Override
    protected boolean areContentsTheSame(Repo oldItem, Repo newItem) {
        return Objects.equals(oldItem.chapters, newItem.chapters);
    }

    public interface RepoClickCallback {
        void onClick(Repo repo);
    }
}
