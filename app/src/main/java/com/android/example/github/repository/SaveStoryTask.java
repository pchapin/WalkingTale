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
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.android.example.github.db.GithubDb;
import com.android.example.github.db.RepoDao;
import com.android.example.github.vo.Repo;
import com.android.example.github.walkingTale.ExampleRepo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * A task that uploads a created story to a remote database.
 */
public class SaveStoryTask implements Runnable {
    private final GithubDb db;
    private final RepoDao repoDao;
    private Repo story;
    private Context context;
    private TransferUtility transferUtility;


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


            // Dynamo DB upload

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
//            dynamoDBMapper.save(repo);

            // S3 upload
            File outputDir = context.getCacheDir(); // context being the Activity pointer
            File file = null;
            try {
                file = File.createTempFile("prefix", "extension", outputDir);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(ExampleRepo.Companion.getRepo().toString());
                Util.getS3Client(context).putObject(Constants.BUCKET_NAME, "ok", file);

                transferUtility = Util.getTransferUtility(context);



            } catch (IOException e) {
                e.printStackTrace();
            }

            db.repoDao().insert(repo);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
