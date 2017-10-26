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

import com.android.example.github.R;
import com.android.example.github.databinding.ChapterItemBinding;
import com.android.example.github.walkingTale.Chapter;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * A RecyclerView adapter for {@link Chapter} class.
 */
public class ChapterAdapter extends DataBoundListAdapter<Chapter, ChapterItemBinding> {
    private final DataBindingComponent dataBindingComponent;
    private final ChapterClickBack chapterClickBack;
    private final boolean showFullName;

    public ChapterAdapter(DataBindingComponent dataBindingComponent,
                          boolean showFullName,
                          ChapterClickBack chapterClickBack
    ) {
        this.dataBindingComponent = dataBindingComponent;
        this.chapterClickBack = chapterClickBack;
        this.showFullName = showFullName;
    }

    @Override
    protected ChapterItemBinding createBinding(ViewGroup parent) {
        ChapterItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.chapter_item,
                        parent, false, dataBindingComponent);
        binding.setShowFullName(showFullName);
        binding.getRoot().setOnClickListener(v -> {
            Chapter chapter = binding.getChapter();
            if (chapter != null && chapterClickBack != null) {
                chapterClickBack.onClick(chapter);
            }
        });
        return binding;
    }

    @Override
    protected void bind(ChapterItemBinding binding, Chapter item) {
        binding.setChapter(item);
    }

    @Override
    protected boolean areItemsTheSame(Chapter oldItem, Chapter newItem) {
        return oldItem.getId() == newItem.getId();
    }

    @Override
    protected boolean areContentsTheSame(Chapter oldItem, Chapter newItem) {
        return oldItem.getExpositions().equals(newItem.getExpositions());
    }

//    @Override
//    protected void bind(RepoItemBinding binding, Repo item) {
//        binding.setRepo(item);
//    }
//
//    @Override
//    protected boolean areItemsTheSame(Repo oldItem, Repo newItem) {
//        return Objects.equals(oldItem.owner, newItem.owner) &&
//                Objects.equals(oldItem.name, newItem.name);
//    }
//
//    @Override
//    protected boolean areContentsTheSame(Repo oldItem, Repo newItem) {
//        return Objects.equals(oldItem.description, newItem.description) &&
//                oldItem.stars == newItem.stars;
//    }

    public interface ChapterClickBack {
        void onClick(Chapter chapter);
    }
}
