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
import com.android.example.github.databinding.ExpositionItemBinding;
import com.android.example.github.util.Objects;
import com.android.example.github.walkingTale.Exposition;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * A RecyclerView adapter for {@link Exposition} class.
 */
public class ExpositionAdapter extends DataBoundListAdapter<Exposition, ExpositionItemBinding> {
    private final DataBindingComponent dataBindingComponent;
    private final ExpositionClickBack expositionClickBack;

    public ExpositionAdapter(DataBindingComponent dataBindingComponent,
                             boolean showFullName,
                             ExpositionClickBack expositionClickBack
    ) {
        this.dataBindingComponent = dataBindingComponent;
        this.expositionClickBack = expositionClickBack;
    }

    @Override
    protected ExpositionItemBinding createBinding(ViewGroup parent) {
        ExpositionItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.exposition_item,
                        parent, false, dataBindingComponent);
        binding.getRoot().setOnClickListener(v -> {
            Exposition exposition = binding.getExposition();
            if (exposition != null && expositionClickBack != null) {
                expositionClickBack.onClick(exposition);
            }
        });
        return binding;
    }

    @Override
    protected void bind(ExpositionItemBinding binding, Exposition item) {
        binding.setExposition(item);
    }

    @Override
    protected boolean areItemsTheSame(Exposition oldItem, Exposition newItem) {
        return Objects.equals(oldItem.getId(), newItem.getId());
    }

    @Override
    protected boolean areContentsTheSame(Exposition oldItem, Exposition newItem) {
        return Objects.equals(oldItem.getId(), newItem.getId());
    }

    public interface ExpositionClickBack {
        void onClick(Exposition exposition);
    }
}
