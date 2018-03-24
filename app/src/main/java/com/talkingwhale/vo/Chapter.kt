package com.talkingwhale.vo

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBDocument
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshaller
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshalling
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson

@DynamoDBDocument
data class Chapter(var posts: ArrayList<Post> = ArrayList(),
                   var name: String,
                   @DynamoDBMarshalling(marshallerClass = LatLngMarshaller::class)
                   var location: LatLng,
                   var id: Int,
                   var radius: Int) {

    // Zero arg constructor needed for ddb mapper
    constructor() : this(arrayListOf<Post>(), "", LatLng(0.0, 0.0), 0, 0)
}


class LatLngMarshaller : DynamoDBMarshaller<LatLng> {
    override fun unmarshall(clazz: Class<LatLng>?, obj: String?): LatLng {
        return Gson().fromJson(obj, clazz)
    }

    override fun marshall(getterReturnResult: LatLng?): String {
        return Gson().toJson(getterReturnResult)
    }
}