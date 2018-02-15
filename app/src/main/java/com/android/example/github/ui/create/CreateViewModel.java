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

import com.android.example.github.MainActivity;
import com.android.example.github.repository.RepoRepository;
import com.android.example.github.util.AbsentLiveData;
import com.android.example.github.vo.Resource;
import com.android.example.github.vo.Story;
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


    private final String TAG = this.getClass().getSimpleName();
    private RepoRepository repoRepository;
    private MutableLiveData<Story> story = null;

    // TODO: 11/22/17 Limit the number of expositions a user can add
    private int expositionCount = 0;
    private int minRadius = 10;
    private int maxRadius = 20;


    @Inject
    public CreateViewModel(RepoRepository repository) {
        this.repoRepository = repository;
        if (story == null) {
            this.story = new MutableLiveData<>();
            Story story = ExampleRepo.Companion.getRepo();
            story.username = MainActivity.getCognitoUsername();
            this.story.setValue(story);
        }
    }

    void setGenre(String genre) {
        Story story = this.story.getValue();
        story.genre = genre;
        this.story.setValue(story);
    }

    void setStoryName(String storyName) {
        Story story = this.story.getValue();
        story.storyName = storyName;
        this.story.setValue(story);
    }

    void setDescription(String description) {
        Story story = this.story.getValue();
        story.description = description;
        this.story.setValue(story);
    }

    void setTags(List<String> tags) {
        Story story = this.story.getValue();
        story.tags = tags;
        this.story.setValue(story);
    }

    void setStoryImage(String image) {
        Story story = this.story.getValue();
        story.story_image = image;
        this.story.setValue(story);
    }

    public LiveData<Story> getStory() {
        return story;
    }

    LiveData<Resource<Void>> finishStory(Context context) {
        // Check that fields of story are valid
        Log.i(TAG, "tags " + story.getValue().tags);
        if (story.getValue().getStoryName().isEmpty()) {
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
        Story story = this.story.getValue();
        story.chapters.add(chapter);
        Log.i("addchapter", "story " + story);
        this.story.setValue(story);
    }

    /**
     * Add an exposition to the latest chapter
     *
     * @param expositionType
     * @param content
     */
    public final void addExposition(@NotNull ExpositionType expositionType, @NotNull String content) {
        int var4 = this.expositionCount++;
        Exposition exposition = new Exposition(expositionType, content, var4);
        Story story = this.story.getValue();
        Chapter chapter = CollectionsKt.last(this.story.getValue().chapters);
        ArrayList<Exposition> expositions = chapter.getExpositions();
        expositions.add(exposition);
        chapter.setExpositions(expositions);
        story.chapters.remove(story.chapters.size() - 1);
        story.chapters.add(chapter);
        this.story.setValue(story);
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
        Story story = this.story.getValue();
        story.chapters.remove(story.chapters.size() - 1);
        this.story.setValue(story);
    }

    public final void incrementRadius() throws ArrayIndexOutOfBoundsException {
        if ((CollectionsKt.last(this.story.getValue().chapters)).getRadius() == this.maxRadius) {
            throw (new ArrayIndexOutOfBoundsException("Max radius size is already " + this.maxRadius));
        } else {
            Chapter chapter = CollectionsKt.last(this.story.getValue().chapters);
            chapter.setRadius(chapter.getRadius() + 1);
            Story story = this.story.getValue();
            story.chapters.remove(story.chapters.size() - 1);
            story.chapters.add(chapter);
            this.story.setValue(story);
        }
    }

    public final void decrementRadius() throws ArrayIndexOutOfBoundsException {
        if ((CollectionsKt.last(this.story.getValue().chapters)).getRadius() == this.minRadius) {
            throw (new ArrayIndexOutOfBoundsException("Min radius size is already " + this.minRadius));
        } else {
            Chapter chapter = CollectionsKt.last(this.story.getValue().chapters);
            chapter.setRadius(chapter.getRadius() - 1);
            Story story = this.story.getValue();
            story.chapters.remove(story.chapters.size() - 1);
            story.chapters.add(chapter);
            this.story.setValue(story);
        }
    }
}
