package com.android.example.github.repository

import android.util.Log
import com.amazonaws.AmazonServiceException
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import com.android.example.github.vo.User


class GetUserTask(val userId: String) : AbstractTask<String, User>(userId) {

    override fun run() {

        try {
            Log.i(TAG, input)
            val re = dynamoDBMapper.scan(User::class.java, DynamoDBScanExpression())
            Log.i(TAG, "$re")
            val item = re.filter { it.userId == userId }
            Log.i(TAG, "$item")

            if (item.isEmpty())
                result.postValue(Resource(Status.ERROR, null, null))
            else
                result.postValue(Resource(Status.SUCCESS, item[0], null))

        } catch (ex: AmazonServiceException) {
            Log.i(TAG, "$ex")
        }

    }
}