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

package com.android.example.github.repository;

import android.content.Context;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.android.example.github.db.GithubDb;
import com.android.example.github.db.RepoDao;
import com.android.example.github.vo.Repo;

/**
 * A task that uploads a created story to a remote database.
 */
public class SaveStoryTask implements Runnable {
    private final GithubDb db;
    private final RepoDao repoDao;
    private Repo story;
    private Context context;

    SaveStoryTask(Repo story, GithubDb db, RepoDao repoDao, Context context) {
        this.db = db;
        this.repoDao = repoDao;
        this.story = story;
        this.context = context;
    }


    @Override
    public void run() {

        try {
            db.beginTransaction();

            Repo repo = (new Repo(Repo.UNKNOWN_ID,
                    story.name, story.description,
                    story.chapters, story.genre, story.tags,
                    story.duration, story.rating, story.chapters.get(0).getLocation().latitude,
                    story.chapters.get(0).getLocation().longitude, story.story_image));

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(new AWSCredentials() {
                @Override
                public String getAWSAccessKeyId() {
                    return "access key here";
                }

                @Override
                public String getAWSSecretKey() {
                    return "secret key here";
                }
            });
            DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(ddbClient);

            dynamoDBMapper.save(repo);

            db.repoDao().insert(repo);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
