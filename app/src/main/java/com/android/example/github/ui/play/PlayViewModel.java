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
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;
import com.android.example.github.walkingTale.Chapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class PlayViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    @VisibleForTesting
    private final MutableLiveData<String> repoId;
    private final LiveData<Resource<Repo>> repo;
    LiveData<List<Chapter>> availableChapters = new MutableLiveData<>();
    private Repo story;
    private Chapter finalChapter;
    private MutableLiveData<Chapter> nextChapter = new MutableLiveData<>();

    @Inject
    PlayViewModel(RepoRepository repository) {
        this.repoId = new MutableLiveData<>();
        repo = Transformations.switchMap(repoId, repository::loadRepo);

        availableChapters = Transformations.map(nextChapter, (Chapter next) -> {
            if (next == null) return Collections.emptyList();

            List<Chapter> chapterList = new ArrayList<>();
            for (Chapter chapter : story.chapters) {
                if (chapter.getId() <= next.getId()) {
                    chapterList.add(chapter);
                }
            }
            return chapterList;
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
        finalChapter = story.chapters.get(story.chapters.size() - 1);
        nextChapter.setValue(story.chapters.get(0));
    }

    boolean isStorySet() {
        return story != null;
    }

    Chapter getNextChapter() {
        return nextChapter.getValue();
    }

    void goToNextChapter() throws ArrayIndexOutOfBoundsException {
        if (nextChapter.getValue() == story.chapters.get(story.chapters.size() - 1)) {
            throw new ArrayIndexOutOfBoundsException("Current chapter is already the last chapter.");
        }
        nextChapter.setValue(story.chapters.get(nextChapter.getValue().getId() + 1));
    }
}
