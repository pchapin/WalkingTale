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

import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.android.example.github.api.GithubService;
import com.android.example.github.db.GithubDb;
import com.android.example.github.db.RepoDao;
import com.android.example.github.vo.Repo;
import com.android.example.github.walkingTale.Story;
import com.google.gson.Gson;


import java.io.File;
import java.lang.reflect.Type;

/**
 * A task that uploads a created story to a remote database.
 */
public class SaveStoryTask implements Runnable {
    private final GithubDb db;
    private final RepoDao repoDao;
    private Story story;
    private TransferUtility transferUtility;

    SaveStoryTask(Story story, GithubDb db, RepoDao repoDao, Context context) {
        this.db = db;
        this.repoDao = repoDao;
        this.story = story;
        transferUtility = Util.getTransferUtility(context);
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        String name = "story name";
        // Current user
        String owner = "me";
        // TODO: 10/27/2017 what should the rating start at?
        String rating = "0";
        String description = "some description";

        try {
            db.beginTransaction();

            File file = new File("blah");
            TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME, file.getName(),
                    file);
            Repo repo = (new Repo(Repo.UNKNOWN_ID,
                    name, owner + "/" + name, description, new Repo.Owner(owner, null),
                    0, story.getChapters(), "", "", "",
                    "", rating, story.getChapters().get(0).getLocation().latitude,
                    story.getChapters().get(0).getLocation().longitude, ""));

            String json = gson.toJson(repo);
            Log.i("repo to json", json);
            Repo repo1 = gson.fromJson(json, (java.lang.reflect.Type) Repo.class);
            Log.i("json to repo", repo1.name);

            db.repoDao().insert(repo);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
