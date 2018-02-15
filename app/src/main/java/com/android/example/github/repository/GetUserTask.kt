package com.android.example.github.repository

import android.util.Log
import com.amazonaws.AmazonServiceException
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.android.example.github.api.GithubService
import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import com.android.example.github.vo.User


class GetUserTask(thing: String, githubService: GithubService) : AbstractTask<String, User>(thing, githubService) {

    override fun run() {
        val response: User? = null

        try {
            val expressionAttributeNames = mutableMapOf<String, String>()
            expressionAttributeNames.put(":userId", "val1")

            val attributeValues = mutableMapOf<String, AttributeValue>()
            attributeValues.put(":from", AttributeValue(input))

            val scanExpression = DynamoDBScanExpression()

            val re = dynamoDBMapper.scan(User::class.java, scanExpression)
            Log.i(TAG, "$re")
//            response = dynamoDBMapper.load(User::class.java, input)

//            val identityManager = IdentityManager.getDefaultIdentityManager()
//            val note = User()
//            note.setUserId(identityManager.getCachedUserID())
//            note.setUserName("todd")
//
//            val rangeKeyCondition = Condition()
//                    .withComparisonOperator(ComparisonOperator.BEGINS_WITH)
//                    .withAttributeValueList(AttributeValue().withS("Trial"))
//
//            val queryExpression = DynamoDBQueryExpression<User>()
//                    .withHashKeyValues(note)
//                    .withRangeKeyCondition("userName", rangeKeyCondition)
//                    .withConsistentRead(false)
//            val result = dynamoDBMapper.query(User::class.java, queryExpression)
//
//            val gson = Gson()
//            val stringBuilder = StringBuilder()
//
//            // Loop through query results
//            for (i in result.indices) {
//                val jsonFormOfItem = gson.toJson(result.get(i))
//                stringBuilder.append(jsonFormOfItem + "\n\n")
//            }
//
//            // Add your code here to deal with the data result
////            updateOutput(stringBuilder.toString())
//
//            if (result.isEmpty()) {
//                // There were no items matching your query.
//            }
//
//            Log.i(TAG, "" + result)

        } catch (ex: AmazonServiceException) {
            Log.i(TAG, "$ex")
        }

        if (response == null)
            result.postValue(Resource(Status.ERROR, response, response))
        else
            result.postValue(Resource(Status.SUCCESS, response, response.toString()))
    }
}