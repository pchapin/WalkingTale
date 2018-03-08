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

package com.MapPost.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.MapPost.vo.Post;

import java.util.List;

/**
 * Interface for database access on Post related operations.
 */
@Dao
public abstract class PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(Post... stories);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertStories(List<Post> repositories);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long createPostIfNotExists(Post Post);

    @Query("SELECT * FROM Post WHERE postId = :id")
    public abstract LiveData<Post> load(String id);

    @Query("SELECT * FROM Post")
    public abstract LiveData<List<Post>> loadAll();

    @Query("SELECT * FROM Post WHERE postId in (:playedIds)")
    public abstract LiveData<List<Post>> loadPlayedStories(List<String> playedIds);

    @Query("SELECT * FROM Post WHERE postId in (:createdIds)")
    public abstract LiveData<List<Post>> loadCreatedStories(List<String> createdIds);
}
