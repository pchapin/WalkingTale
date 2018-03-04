package com.walkingtale.vo

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBDocument
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshaller
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshalling
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.clustering.ClusterItem

@DynamoDBDocument
data class Exposition(
        @DynamoDBMarshalling(marshallerClass = ExpositionTypeMarshaller::class)
        var type: ExpositionType,
        var content: String,
        var id: Int,
        var latLng: LatLng?) : ClusterItem {
    override fun getSnippet(): String {
        return latLng!!.latitude.toString()
    }

    override fun getTitle(): String {
        return latLng!!.longitude.toString()
    }

    override fun getPosition(): LatLng {
        return latLng!!
    }

    // Zero arg constructor needed for ddb mapper
    constructor() : this(ExpositionType.TEXT, "", 0, null)

    constructor(type: ExpositionType, content: String, id: Int) : this(type, content, id, null)
}

class ExpositionTypeMarshaller : DynamoDBMarshaller<ExpositionType> {
    override fun unmarshall(clazz: Class<ExpositionType>?, obj: String?): ExpositionType {
        return Gson().fromJson(obj, clazz)
    }

    override fun marshall(getterReturnResult: ExpositionType?): String {
        return Gson().toJson(getterReturnResult)
    }
}

enum class ExpositionType {
    TEXT, AUDIO, PICTURE
}