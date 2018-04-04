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

package com.talkingwhale.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

import com.talkingwhale.vo.Post

/**
 * Interface for database access on Post related operations.
 */
@Dao
abstract class PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(vararg stories: Post)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertStories(repositories: List<Post>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun createPostIfNotExists(Post: Post): Long

    @Query("SELECT * FROM Post WHERE postId = :id")
    abstract fun load(id: String): LiveData<Post>

    @Query("SELECT * FROM Post")
    abstract fun loadAll(): LiveData<List<Post>>

    @Query("SELECT * FROM Post WHERE postId in (:playedIds)")
    abstract fun loadPlayedStories(playedIds: List<String>): LiveData<List<Post>>

    @Query("SELECT * FROM Post WHERE postId in (:createdIds)")
    abstract fun loadCreatedStories(createdIds: List<String>): LiveData<List<Post>>

    @Delete()
    abstract fun delete(post: Post)
}
