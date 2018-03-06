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

package com.WalkingTale.di;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.WalkingTale.api.WalkingTaleService;
import com.WalkingTale.db.StoryDao;
import com.WalkingTale.db.UserDao;
import com.WalkingTale.db.WalkingTaleDb;
import com.WalkingTale.util.LiveDataCallAdapterFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module(includes = ViewModelModule.class)
class AppModule {
    @Singleton @Provides
    WalkingTaleService provideGithubService() {
        // was https://api.github.com/
        return new Retrofit.Builder()
                .baseUrl("https://yf5r4d4qsc.execute-api.us-east-1.amazonaws.com/dev/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .build()
                .create(WalkingTaleService.class);
    }

    @Singleton @Provides
    WalkingTaleDb provideDb(Application app) {
        return Room.databaseBuilder(app, WalkingTaleDb.class, "github.db").fallbackToDestructiveMigration().build();
    }

    @Singleton @Provides
    UserDao provideUserDao(WalkingTaleDb db) {
        return db.userDao();
    }

    @Singleton @Provides
    StoryDao provideRepoDao(WalkingTaleDb db) {
        return db.storyDao();
    }
}
