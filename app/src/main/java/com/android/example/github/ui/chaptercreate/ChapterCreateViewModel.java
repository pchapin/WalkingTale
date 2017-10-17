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

package com.android.example.github.ui.chaptercreate;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.support.annotation.VisibleForTesting;
import android.text.Editable;

import com.android.example.github.repository.RepoRepository;
import com.android.example.github.util.AbsentLiveData;
import com.android.example.github.util.Objects;
import com.android.example.github.vo.Contributor;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;
import com.android.example.github.walkingTale.Chapter;
import com.android.example.github.walkingTale.Exposition;
import com.android.example.github.walkingTale.ExpositionType;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ChapterCreateViewModel extends ViewModel {

    private ArrayList<Exposition> expositions = new ArrayList<>();
    private Chapter chapter = new Chapter(expositions, "", new Location(""));

    @Inject
    public ChapterCreateViewModel(RepoRepository repository) {

    }


    public void addTextExposition(Editable textExposition) {
        expositions.add(new Exposition(ExpositionType.TEXT, textExposition.toString()));
    }


    public Chapter getChapter() {
        return chapter;
    }
}
