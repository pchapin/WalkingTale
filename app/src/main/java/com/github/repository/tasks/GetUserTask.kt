package com.github.repository.tasks

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.github.db.GithubDb
import com.github.vo.Resource
import com.github.vo.Status
import com.github.vo.User

class GetUserTask(val userId: String, val db: GithubDb) : AbstractTask<String, User>(userId, db) {

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