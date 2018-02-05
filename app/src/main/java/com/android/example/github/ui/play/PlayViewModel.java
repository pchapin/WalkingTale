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

package com.android.example.github.ui.play;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.VisibleForTesting;

import com.android.example.github.repository.RepoRepository;
import com.android.example.github.util.AbsentLiveData;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;
import com.android.example.github.walkingTale.Chapter;

import javax.inject.Inject;

public class PlayViewModel extends ViewModel {
    @VisibleForTesting
    private final MutableLiveData<String> repoId;
    private final LiveData<Resource<Repo>> repo;
    private Repo story;
    private Chapter currentChapter;

    @Inject
    PlayViewModel(RepoRepository repository) {
        this.repoId = new MutableLiveData<>();
        repo = Transformations.switchMap(repoId, input -> {
            if (input == null) {
                return AbsentLiveData.create();
            }
            return repository.loadRepo(input);
        });
    }

    public LiveData<Resource<Repo>> getRepo() {
        return repo;
    }

    void setId(String id) {
        repoId.setValue(id);
    }

    void setStory(Repo repo) throws IllegalArgumentException {
        if (story != null) throw new IllegalArgumentException("Story has already been initialized");
        story = repo;
        currentChapter = story.chapters.get(0);
    }

    boolean isStorySet() {
        return story != null;
    }

    Chapter getCurrentChapter() {
        return currentChapter;
    }

    void goToNextChapter() throws ArrayIndexOutOfBoundsException {
        if (currentChapter == story.chapters.get(story.chapters.size() - 1)) {
            throw new ArrayIndexOutOfBoundsException("Current chapter is already the last chapter.");
        }
        currentChapter = story.chapters.get(currentChapter.getId() + 1);
    }
}
