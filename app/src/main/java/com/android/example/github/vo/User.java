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
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.android.example.github.db.GithubTypeConverters;

import java.util.List;

@Entity(primaryKeys = "userId")
@TypeConverters(GithubTypeConverters.class)
@DynamoDBTable(tableName = "walkingtale-mobilehub-466729221-Users")
public class User {

    @NonNull
    @DynamoDBHashKey(attributeName = "userId")
    public String userId;
    @DynamoDBAttribute(attributeName = "createdStories")
    public List<String> createdStories;
    @DynamoDBAttribute(attributeName = "playedStories")
    public List<String> playedStories;
    @DynamoDBAttribute(attributeName = "userName")
    @DynamoDBIndexRangeKey(attributeName = "userName")
    public String userName;
    @DynamoDBAttribute(attributeName = "userImage")
    public String userImage;

    public User() {
    }

    public User(@NonNull String userId, List<String> createdStories, List<String> playedStories, String userName, String userImage) {
        this.userId = userId;
        this.createdStories = createdStories;
        this.playedStories = playedStories;
        this.userName = userName;
        this.userImage = userImage;
    }

    @NonNull
    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @DynamoDBAttribute(attributeName = "createdStories")
    public List<String> getCreatedStories() {
        return createdStories;
    }

    public void setCreatedStories(List<String> createdStories) {
        this.createdStories = createdStories;
    }

    @DynamoDBAttribute(attributeName = "playedStories")
    public List<String> getPlayedStories() {
        return playedStories;
    }

    public void setPlayedStories(List<String> playedStories) {
        this.playedStories = playedStories;
    }

    @DynamoDBRangeKey(attributeName = "userName")
    @DynamoDBAttribute(attributeName = "userName")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @DynamoDBAttribute(attributeName = "userImage")
    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }
}
