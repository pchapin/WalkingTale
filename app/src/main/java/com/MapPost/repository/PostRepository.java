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

package com.MapPost.repository;

import com.MapPost.AppExecutors;
import com.MapPost.api.WalkingTaleService;
import com.MapPost.db.PostDao;
import com.MapPost.db.WalkingTaleDb;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PostRepository {

    private final String TAG = this.getClass().getSimpleName();
    private final WalkingTaleDb db;
    private final PostDao storyDao;
    private final WalkingTaleService walkingTaleService;
    private final AppExecutors appExecutors;

    @Inject
    public PostRepository(AppExecutors appExecutors, WalkingTaleDb db, PostDao storyDao,
                          WalkingTaleService walkingTaleService) {
        this.db = db;
        this.storyDao = storyDao;
        this.walkingTaleService = walkingTaleService;
        this.appExecutors = appExecutors;
    }
}
