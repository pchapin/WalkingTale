package com.talkingwhale.repository.tasks

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.talkingwhale.db.AppDatabase
import com.talkingwhale.vo.Resource
import com.talkingwhale.vo.Status
import com.talkingwhale.vo.User

class GetUserTask(val userId: String, val db: AppDatabase) : AbstractTask<String, User>(userId) {

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