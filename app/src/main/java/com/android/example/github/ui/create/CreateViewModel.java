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
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.example.github.repository.RepoRepository;
import com.android.example.github.util.AbsentLiveData;
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

    // TODO: 12/17/17 turn all fields into live data, and only create repo right at end

    private RepoRepository repoRepository;
    private MutableLiveData<Repo> story = null;

    // TODO: 11/22/17 Limit the number of expositions a user can add
    private int expositionCount = 0;
    private int minRadius = 10;
    private int maxRadius = 20;


    @Inject
    public CreateViewModel(RepoRepository repository) {
        this.repoRepository = repository;
        if (story == null) {
            story = new MutableLiveData<>();
            story.setValue(ExampleRepo.Companion.getRepo());
        }
    }

    void setGenre(String genre) {
        Repo repo = story.getValue();
        repo.genre = genre;
        story.setValue(repo);
    }

    void setStoryName(String storyName) {
        Repo repo = story.getValue();
        repo.name = storyName;
        story.setValue(repo);
    }

    void setDescription(String description) {
        Repo repo = story.getValue();
        repo.description = description;
        story.setValue(repo);
    }

    void setTags(String tags) {
        Repo repo = story.getValue();
        repo.tags = tags;
        story.setValue(repo);
    }

    void setStoryImage(String image) {
        Repo repo = story.getValue();
        repo.story_image = image;
        story.setValue(repo);
    }

    public LiveData<Repo> getStory() {
        return story;
    }

    LiveData<Boolean> finishStory(Context context) {
        // Check that fields of story are valid
        if (story.getValue().name.isEmpty()) {
            Toast.makeText(context, "Please enter a name.", Toast.LENGTH_SHORT).show();
        } else if (story.getValue().genre.isEmpty()) {
            Toast.makeText(context, "Please select a genre.", Toast.LENGTH_SHORT).show();
        } else if (story.getValue().description.isEmpty()) {
            Toast.makeText(context, "Please enter a description.", Toast.LENGTH_SHORT).show();
        } else if (story.getValue().tags.isEmpty()) {
            Toast.makeText(context, "Please enter up to 5 tags.", Toast.LENGTH_SHORT).show();
        } else if (story.getValue().story_image.isEmpty()) {
            Toast.makeText(context, "Please select a image for your story.", Toast.LENGTH_SHORT).show();
        } else {
            return repoRepository.publishStory(story.getValue());
        }
        return AbsentLiveData.create();
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
        Repo repo = story.getValue();
        Chapter chapter = CollectionsKt.last(this.story.getValue().chapters);
        ArrayList<Exposition> expositions = chapter.getExpositions();
        expositions.add(exposition);
        chapter.setExpositions(expositions);
        repo.chapters.remove(repo.chapters.size() - 1);
        repo.chapters.add(chapter);
        story.setValue(repo);
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
        Repo repo = story.getValue();
        repo.chapters.remove(repo.chapters.size() - 1);
        story.setValue(repo);
    }

    public final void incrementRadius() throws ArrayIndexOutOfBoundsException {
        if ((CollectionsKt.last(this.story.getValue().chapters)).getRadius() == this.maxRadius) {
            throw (new ArrayIndexOutOfBoundsException("Max radius size is already " + this.maxRadius));
        } else {
            Chapter chapter = CollectionsKt.last(this.story.getValue().chapters);
            chapter.setRadius(chapter.getRadius() + 1);
            Repo repo = story.getValue();
            repo.chapters.remove(repo.chapters.size() - 1);
            repo.chapters.add(chapter);
            story.setValue(repo);
        }
    }

    public final void decrementRadius() throws ArrayIndexOutOfBoundsException {
        if ((CollectionsKt.last(this.story.getValue().chapters)).getRadius() == this.minRadius) {
            throw (new ArrayIndexOutOfBoundsException("Min radius size is already " + this.minRadius));
        } else {
            Chapter chapter = CollectionsKt.last(this.story.getValue().chapters);
            chapter.setRadius(chapter.getRadius() - 1);
            Repo repo = story.getValue();
            repo.chapters.remove(repo.chapters.size() - 1);
            repo.chapters.add(chapter);
            story.setValue(repo);
        }
    }
}
