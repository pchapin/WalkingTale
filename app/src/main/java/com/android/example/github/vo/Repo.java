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

package com.android.example.github.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.android.example.github.db.GithubTypeConverters;
import com.android.example.github.walkingTale.Chapter;
import com.android.example.github.walkingTale.ExampleRepo;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

/**
 * Using name/owner_login as primary key instead of id since name/owner_login is always available
 * vs id is not.
 */
@Entity(indices = {@Index("id")},
        primaryKeys = {"name"})
@TypeConverters(GithubTypeConverters.class)
@DynamoDBTable(tableName = "Repo")
public class Repo {
    public static final int UNKNOWN_ID = -1;
    public String id;
    @SerializedName("name")
    @NonNull
    public String name;
    @SerializedName("description")
    public String description;
    @SerializedName("chapters")
    public List<Chapter> chapters;
    @SerializedName("genre")
    public String genre;
    @SerializedName("tags")
    public String tags;
    @SerializedName("duration")
    public int duration;
    @SerializedName("rating")
    public Double rating;
    @SerializedName("latitude")
    public Double latitude;
    @SerializedName("longitude")
    public Double longitude;
    @SerializedName("story_image")
    public String story_image;

    public Repo(String id, @NonNull String name, String description, List<Chapter> chapters, String genre, String tags, int duration, Double rating, Double latitude, Double longitude, String story_image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.chapters = chapters;
        this.genre = genre;
        this.tags = tags;
        this.duration = duration;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
        this.story_image = story_image;
    }

    @Ignore
    public Repo() {
        Repo exampleRepo = ExampleRepo.Companion.getRepo();
        this.id = exampleRepo.id;
        this.name = exampleRepo.name;
        this.description = exampleRepo.description;
        this.chapters = exampleRepo.chapters;
        this.genre = exampleRepo.genre;
        this.tags = exampleRepo.tags;
        this.duration = exampleRepo.duration;
        this.rating = exampleRepo.rating;
        this.latitude = exampleRepo.latitude;
        this.longitude = exampleRepo.longitude;
        this.story_image = exampleRepo.story_image;
    }

    /**
     * @param json A repo in json form
     * @return A repo
     */
    public static Repo fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Repo.class);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Repo) {
            if (((Repo) o).chapters.equals(this.chapters)) {
                if (Objects.equals(((Repo) o).id, this.id)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return The json representation of this repo
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setStory_image(String story_image) {
        this.story_image = story_image;
    }
}
