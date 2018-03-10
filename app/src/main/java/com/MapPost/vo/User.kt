package com.MapPost.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.TypeConverters
import android.support.annotation.NonNull
import com.MapPost.db.AppTypeConverters
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable

@DynamoDBTable(tableName = "mappost-mobilehub-452475001-Users")
@Entity(indices = [(Index("userId"))], primaryKeys = ["userId"])
@TypeConverters(AppTypeConverters::class)
data class User(
        @get:DynamoDBHashKey(attributeName = "userId")
        @get:DynamoDBAttribute(attributeName = "userId")
        @NonNull
        var userId: String,
        @get:DynamoDBRangeKey(attributeName = "userName")
        @get:DynamoDBAttribute(attributeName = "userName")
        var userName: String,
        @get:DynamoDBAttribute(attributeName = "createdPosts")
        var createdPosts: MutableList<String>,
        @get:DynamoDBAttribute(attributeName = "userImage")
        var userImage: String,
        @get:DynamoDBAttribute(attributeName = "viewedPosts")
        var viewedPosts: MutableList<String>
) {
    // Just to make DDB mapper happy
    constructor() : this("none", "none", mutableListOf(), "none", mutableListOf())
}