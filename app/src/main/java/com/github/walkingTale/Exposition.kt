package com.github.walkingTale

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBDocument
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshaller
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshalling
import com.google.gson.Gson

@DynamoDBDocument
data class Exposition(
        @DynamoDBMarshalling(marshallerClass = ExpositionTypeMarshaller::class)
        var type: ExpositionType,
        var content: String,
        var id: Int) {

    // Zero arg constructor needed for ddb mapper
    constructor() : this(ExpositionType.TEXT, "", 0)
}

class ExpositionTypeMarshaller : DynamoDBMarshaller<ExpositionType> {
    override fun unmarshall(clazz: Class<ExpositionType>?, obj: String?): ExpositionType {
        return Gson().fromJson(obj, clazz)
    }

    override fun marshall(getterReturnResult: ExpositionType?): String {
        return Gson().toJson(getterReturnResult)
    }
}