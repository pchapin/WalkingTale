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

package com.android.example.github.ui.create;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.android.example.github.repository.RepoRepository;
import com.android.example.github.vo.Repo;
import com.android.example.github.walkingTale.Chapter;
import com.android.example.github.walkingTale.ExampleRepo;
import com.android.example.github.walkingTale.Exposition;
import com.android.example.github.walkingTale.ExpositionType;
import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import kotlin.collections.CollectionsKt;

public class CreateViewModel extends ViewModel {


    private RepoRepository repoRepository;
    private MutableLiveData<Repo> story = null;

    private int expositionCount = 0;
    private int minRadius = 1;
    private int maxRadius = 10;


    @Inject
    public CreateViewModel(RepoRepository repository) {
        this.repoRepository = repository;
        if (story == null) {
            story = new MutableLiveData<>();
            story.setValue(ExampleRepo.Companion.getRepo());
        }
    }

    public LiveData<Repo> getStory() {
        return story;
    }

    void finishStory() {
        repoRepository.publishStory(story.getValue());
    }

    public final void addChapter(@NotNull String name, @NotNull LatLng location, int radius) {
        Chapter chapter = new Chapter(new ArrayList(), name, location, this.story.getValue().chapters.size(), radius);
        Repo repo = story.getValue();
        repo.chapters.add(chapter);
        Log.i("addchapter", "repo " + repo);
        story.setValue(repo);
    }

    public final void addExposition(@NotNull ExpositionType expositionType, @NotNull String content) {
        int var4 = this.expositionCount++;
        Exposition exposition = new Exposition(expositionType, content, var4);
        (CollectionsKt.last(this.story.getValue().chapters)).getExpositions().add(exposition);
        Repo repo = story.getValue();
        Chapter chapter = CollectionsKt.last(this.story.getValue().chapters);

    }

    @NotNull
    public final List<Chapter> getAllChapters() {
        return this.story.getValue().chapters;
    }

    @NotNull
    public final Chapter getLatestChapter() {
        return CollectionsKt.last(this.story.getValue().chapters);
    }

    public final void removeChapter() throws ArrayIndexOutOfBoundsException {
//        this.story.getValue().chapters.remove(this.story.getValue().chapters.size() - 1);
        Repo repo = story.getValue();
        repo.chapters.remove(repo.chapters.size() - 1);
        story.setValue(repo);
    }

    public final void incrementRadius() throws ArrayIndexOutOfBoundsException {
        if ((CollectionsKt.last(this.story.getValue().chapters)).getRadius() == this.maxRadius) {
            throw (new ArrayIndexOutOfBoundsException("Max radius size is already " + this.maxRadius));
        } else {
            Chapter var10000 = CollectionsKt.last(this.story.getValue().chapters);
            var10000.setRadius(var10000.getRadius() + 1);
        }
    }

    public final void decrementRadius() throws ArrayIndexOutOfBoundsException {
        if ((CollectionsKt.last(this.story.getValue().chapters)).getRadius() == this.minRadius) {
            throw (new ArrayIndexOutOfBoundsException("Min radius size is already " + this.minRadius));
        } else {
            Chapter var10000 = CollectionsKt.last(this.story.getValue().chapters);
            var10000.setRadius(var10000.getRadius() + -1);
        }
    }
}
