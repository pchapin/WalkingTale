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

import com.android.example.github.repository.RepoRepository;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;
import com.android.example.github.vo.Status;
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


    //    public StoryCreateManager storyManager = new StoryCreateManager();
    private RepoRepository repoRepository;
    private MutableLiveData<Resource<Repo>> repo = null;

    private Repo story = ExampleRepo.Companion.getRepo();
    private int expositionCount = 0;
    private int minRadius = 1;
    private int maxRadius = 10;


    @Inject
    public CreateViewModel(RepoRepository repository) {
        this.repoRepository = repository;
        if (repo == null) {
            repo = new MutableLiveData<>();
        }
    }

    private void updateRepo() {
        repo.setValue(new Resource<>(Status.SUCCESS, null, ""));
    }

    public LiveData<Resource<Repo>> getRepo() {
        return repo;
    }

    void finishStory() {
        repoRepository.publishStory(story);
    }

    @NotNull
    public final Repo getStory() {
        return this.story;
    }

    public final void addChapter(@NotNull String name, @NotNull LatLng location, int radius) {
        Chapter chapter = new Chapter(new ArrayList(), name, location, this.story.chapters.size(), radius);
        this.story.chapters.add(chapter);
    }

    public final void addExposition(@NotNull ExpositionType expositionType, @NotNull String content) {
        int var4 = this.expositionCount++;
        Exposition exposition = new Exposition(expositionType, content, var4);
        (CollectionsKt.last(this.story.chapters)).getExpositions().add(exposition);
    }

    @NotNull
    public final List<Chapter> getAllChapters() {
        return this.story.chapters;
    }

    @NotNull
    public final Chapter getLatestChapter() {
        return CollectionsKt.last(this.story.chapters);
    }

    public final void removeChapter() {
        this.story.chapters.remove(this.story.chapters.size() - 1);
    }

    public final void incrementRadius() throws ArrayIndexOutOfBoundsException {
        if ((CollectionsKt.last(this.story.chapters)).getRadius() == this.maxRadius) {
            throw (new ArrayIndexOutOfBoundsException("Max radius size is already " + this.maxRadius));
        } else {
            Chapter var10000 = CollectionsKt.last(this.story.chapters);
            var10000.setRadius(var10000.getRadius() + 1);
        }
    }

    public final void decrementRadius() throws ArrayIndexOutOfBoundsException {
        if ((CollectionsKt.last(this.story.chapters)).getRadius() == this.minRadius) {
            throw (new ArrayIndexOutOfBoundsException("Min radius size is already " + this.minRadius));
        } else {
            Chapter var10000 = CollectionsKt.last(this.story.chapters);
            var10000.setRadius(var10000.getRadius() + -1);
        }
    }
}
