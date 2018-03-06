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

package com.WalkingTale.ui.common;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.WalkingTale.R;
import com.WalkingTale.databinding.ItemChapterBinding;
import com.WalkingTale.util.Objects;
import com.WalkingTale.vo.Chapter;

/**
 * A RecyclerView adapter for {@link Chapter} class.
 */
public class ChapterAdapter extends DataBoundListAdapter<Chapter, ItemChapterBinding> {
    private final DataBindingComponent dataBindingComponent;
    private final ChapterClickBack chapterClickBack;

    public ChapterAdapter(DataBindingComponent dataBindingComponent, ChapterClickBack chapterClickBack) {
        this.dataBindingComponent = dataBindingComponent;
        this.chapterClickBack = chapterClickBack;
    }

    @Override
    protected ItemChapterBinding createBinding(ViewGroup parent) {
        ItemChapterBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.item_chapter,
                        parent, false, dataBindingComponent);
        binding.getRoot().setOnClickListener(v -> {
            Chapter chapter = binding.getChapter();
            if (chapter != null && chapterClickBack != null) {
                chapterClickBack.onClick(chapter);
            }
            new BetterSnapper().attachToRecyclerView(binding.expositionList);

        });
        new BetterSnapper().attachToRecyclerView(binding.expositionList);
        return binding;
    }

    @Override
    protected void bind(ItemChapterBinding binding, Chapter item) {
        binding.setChapter(item);

        ExpositionAdapter expositionAdapter = new ExpositionAdapter(dataBindingComponent, null);
        binding.expositionList.setAdapter(expositionAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(null, LinearLayoutManager.HORIZONTAL, false);
        binding.expositionList.setLayoutManager(linearLayoutManager);
        expositionAdapter.replace(binding.getChapter().getExpositions());
    }

    @Override
    protected boolean areItemsTheSame(Chapter oldItem, Chapter newItem) {
        return Objects.equals(oldItem.getId(), newItem.getId());
    }

    @Override
    protected boolean areContentsTheSame(Chapter oldItem, Chapter newItem) {
        return Objects.equals(oldItem.getId(), newItem.getId());
    }

    public interface ChapterClickBack {
        void onClick(Chapter chapter);
    }
}
