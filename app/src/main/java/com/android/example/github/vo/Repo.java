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

import com.google.gson.annotations.SerializedName;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;

/**
 * Using name/owner_login as primary key instead of id since name/owner_login is always available
 * vs id is not.
 */
@Entity(indices = {@Index("id"), @Index("owner_login")},
        primaryKeys = {"name", "owner_login"})
public class Repo {
    public static final int UNKNOWN_ID = -1;
    public final int id;
    @SerializedName("name")
    public final String name;
    @SerializedName("full_name")
    public final String fullName;
    @SerializedName("description")
    public final String description;
    @SerializedName("stargazers_count")
    public final int stars;
    @SerializedName("owner")
    @Embedded(prefix = "owner_")
    public final Owner owner;
    @SerializedName("chapters")
    public final String chapters;
    @SerializedName("expositions")
    public final String expositions;
    @SerializedName("genre")
    public final String genre;
    @SerializedName("tags")
    public final String tags;
    @SerializedName("duration")
    public final String duration;
    @SerializedName("rating")
    public final String rating;
    @SerializedName("latitude")
    public final Double latitude;
    @SerializedName("longitude")
    public final Double longitude;

    public Repo(int id, String name, String fullName, String description, Owner owner, int stars, String chapters, String expositions, String genre, String tags, String duration, String rating, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.owner = owner;
        this.stars = stars;
        this.chapters = chapters;
        this.expositions = expositions;
        this.genre = genre;
        this.tags = tags;
        this.duration = duration;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static class Owner {
        @SerializedName("login")
        public final String login;
        @SerializedName("url")
        public final String url;

        public Owner(String login, String url) {
            this.login = login;
            this.url = url;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Owner owner = (Owner) o;

            if (login != null ? !login.equals(owner.login) : owner.login != null) {
                return false;
            }
            return url != null ? url.equals(owner.url) : owner.url == null;
        }

        @Override
        public int hashCode() {
            int result = login != null ? login.hashCode() : 0;
            result = 31 * result + (url != null ? url.hashCode() : 0);
            return result;
        }
    }

    //Todo: add these to repo
//    public static class Chapter {
//        public final ArrayList<Exposition> expositions;
//        public final String location;
//        public final String name;
//
//        public Chapter(ArrayList<Exposition> expositions, String location, String name) {
//            this.expositions = expositions;
//            this.location = location;
//            this.name = name;
//        }
//    }
//
//    public static class Exposition {
//        public final String type;
//        public final String contentUrl;
//
//        public Exposition(String type, String url) {
//            this.type = type;
//            this.contentUrl = url;
//        }
//    }
}
