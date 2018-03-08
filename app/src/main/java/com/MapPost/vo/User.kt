package com.MapPost.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.TypeConverters
import android.support.annotation.NonNull
import com.MapPost.db.WalkingTaleTypeConverters
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable

@DynamoDBTable(tableName = "mappost-mobilehub-452475001-Users")
@Entity(indices = [(Index("userId"))], primaryKeys = ["userId"])
@TypeConverters(WalkingTaleTypeConverters::class)
class User {
    @get:DynamoDBHashKey(attributeName = "userId")
    @get:DynamoDBAttribute(attributeName = "userId")
    @NonNull
    var userId: String? = null
    @get:DynamoDBRangeKey(attributeName = "userName")
    @get:DynamoDBAttribute(attributeName = "userName")
    var userName: String? = null
    @get:DynamoDBAttribute(attributeName = "createdPosts")
    var createdPosts: List<String>? = null
    @get:DynamoDBAttribute(attributeName = "userImage")
    var userImage: String? = null
    @get:DynamoDBAttribute(attributeName = "viewedPosts")
    var viewedPosts: List<String>? = null
}