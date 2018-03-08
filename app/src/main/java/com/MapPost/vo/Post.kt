package com.MapPost.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.support.annotation.NonNull
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*
import com.google.gson.Gson


@DynamoDBTable(tableName = "mappost-mobilehub-452475001-Posts")
@Entity(indices = [(Index("userId"))], primaryKeys = ["userId"])
class Post {
    @get:DynamoDBHashKey(attributeName = "userId")
    @get:DynamoDBAttribute(attributeName = "userId")
    @NonNull
    var userId: String? = null
    @get:DynamoDBRangeKey(attributeName = "postId")
    @get:DynamoDBAttribute(attributeName = "postId")
    var postId: String? = null
    @get:DynamoDBAttribute(attributeName = "dateTime")
    var dateTime: String? = null
    @get:DynamoDBIndexHashKey(attributeName = "latitude", globalSecondaryIndexName = "latitude")
    @get:DynamoDBIndexRangeKey(attributeName = "latitude", globalSecondaryIndexName = "longitude")
    var latitude: Double? = null
    @get:DynamoDBIndexHashKey(attributeName = "longitude", globalSecondaryIndexName = "longitude")
    @get:DynamoDBIndexRangeKey(attributeName = "longitude", globalSecondaryIndexName = "latitude")
    var longitude: Double? = null
    @get:DynamoDBAttribute(attributeName = "tags")
    var tags: List<String>? = null
    @get:DynamoDBAttribute(attributeName = "type")
    @DynamoDBMarshalling(marshallerClass = PostTypeMarshaller::class)
    var type: PostType? = null
    @get:DynamoDBAttribute(attributeName = "content")
    var content: String? = null
}

class PostTypeMarshaller : DynamoDBMarshaller<PostType> {
    override fun unmarshall(clazz: Class<PostType>?, obj: String?): PostType {
        return Gson().fromJson(obj, clazz)
    }

    override fun marshall(getterReturnResult: PostType?): String {
        return Gson().toJson(getterReturnResult)
    }
}

enum class PostType {
    TEXT, AUDIO, PICTURE
}