package com.MapPost.repository.tasks

import com.MapPost.db.AppDatabase
import com.MapPost.vo.Resource
import com.MapPost.vo.Status
import com.MapPost.vo.User
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression

class GetUserTask(val userId: String, val db: AppDatabase) : AbstractTask<String, User>(userId, db) {

    override fun run() {
        val response = dynamoDBMapper.scan(User::class.java, DynamoDBScanExpression())
                .filter { it.userId == userId }
        if (response.isEmpty())
            result.postValue(Resource(Status.ERROR, null, ""))
        else {
            result.postValue(Resource(Status.SUCCESS, response[0], null))
            db.beginTransaction()
            db.userDao().insert(response[0])
            db.endTransaction()
        }
    }
}