package com.android.example.github.repository.tasks

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import com.android.example.github.vo.User

class GetUserTask(val userId: String) : AbstractTask<String, User>(userId) {

    override fun run() {
        val response = dynamoDBMapper.scan(User::class.java, DynamoDBScanExpression())
                .filter { it.userId == userId }
        if (response.isEmpty())
            result.postValue(Resource(Status.ERROR, null, ""))
        else
            result.postValue(Resource(Status.SUCCESS, response[0], null))
    }
}