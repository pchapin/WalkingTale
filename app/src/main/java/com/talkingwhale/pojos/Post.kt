package com.talkingwhale.pojos

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.TypeConverters
import android.support.annotation.NonNull
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.clustering.ClusterItem
import com.talkingwhale.R
import com.talkingwhale.activities.MainActivity
import com.talkingwhale.db.AppTypeConverters


@DynamoDBTable(tableName = "mappost-mobilehub-452475001-Posts")
@Entity(indices = [(Index("postId"))], primaryKeys = ["postId"])
@TypeConverters(AppTypeConverters::class)
data class Post(
        @get:DynamoDBHashKey(attributeName = "userId")
        @get:DynamoDBAttribute(attributeName = "userId")
        @NonNull
        var userId: String,
        @get:DynamoDBRangeKey(attributeName = "postId")
        @get:DynamoDBAttribute(attributeName = "postId")
        var postId: String,
        @get:DynamoDBAttribute(attributeName = "dateTime")
        var dateTime: String,
        @get:DynamoDBIndexHashKey(attributeName = "latitude", globalSecondaryIndexName = "latitude")
        @get:DynamoDBIndexRangeKey(attributeName = "latitude", globalSecondaryIndexName = "longitude")
        var latitude: Double,
        @get:DynamoDBIndexHashKey(attributeName = "longitude", globalSecondaryIndexName = "longitude")
        @get:DynamoDBIndexRangeKey(attributeName = "longitude", globalSecondaryIndexName = "latitude")
        var longitude: Double,
        @get:DynamoDBAttribute(attributeName = "tags")
        var tags: MutableList<String>,
        @get:DynamoDBAttribute(attributeName = "type")
        @DynamoDBMarshalling(marshallerClass = PostTypeMarshaller::class)
        var type: PostType,
        @get:DynamoDBAttribute(attributeName = "content")
        var content: String,
        @get:DynamoDBAttribute(attributeName = "linkedPosts")
        var linkedPosts: MutableList<String>
) : ClusterItem {
    @DynamoDBIgnore
    override fun getSnippet(): String {
        return ""
    }

    @DynamoDBIgnore
    override fun getTitle(): String {
        return ""
    }

    @DynamoDBIgnore
    override fun getPosition(): LatLng {
        return LatLng(latitude, longitude)
    }

    /**
     * Do not use this constructor!
     * Just to make DDB mapper happy
     * */
    constructor() : this(MainActivity.getRandomPostId(), MainActivity.getRandomPostId(), "", 0.0, 0.0, mutableListOf(), PostType.TEXT, "", mutableListOf())
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
    TEXT, AUDIO, PICTURE, VIDEO
}

fun getDrawableForPost(post: Post): Int {
    return when (post.type) {
        PostType.TEXT -> R.drawable.ic_textsms_black_24dp
        PostType.AUDIO -> R.drawable.ic_audiotrack_black_24dp
        PostType.PICTURE -> R.drawable.ic_camera_alt_black_24dp
        PostType.VIDEO -> R.drawable.ic_videocam_black_24dp
    }
}