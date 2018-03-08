package com.MapPost.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.MapPost.db.WalkingTaleTypeConverters;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;

@DynamoDBTable(tableName = "mappost-mobilehub-452475001-Users")
@Entity(indices = {@Index("userId")}, primaryKeys = {"userId"})
@TypeConverters(WalkingTaleTypeConverters.class)
public class User {
    @NonNull
    private String userId;
    private String userName;
    private List<String> createdPosts;
    private String userImage;
    private String viewedPosts;

    @NonNull
    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull final String userId) {
        this.userId = userId;
    }

    @DynamoDBRangeKey(attributeName = "userName")
    @DynamoDBAttribute(attributeName = "userName")
    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    @DynamoDBAttribute(attributeName = "createdPosts")
    public List<String> getCreatedPosts() {
        return createdPosts;
    }

    public void setCreatedPosts(final List<String> createdPosts) {
        this.createdPosts = createdPosts;
    }

    @DynamoDBAttribute(attributeName = "userImage")
    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(final String userImage) {
        this.userImage = userImage;
    }

    @DynamoDBAttribute(attributeName = "viewedPosts")
    public String getViewedPosts() {
        return viewedPosts;
    }

    public void setViewedPosts(final String viewedPosts) {
        this.viewedPosts = viewedPosts;
    }

}