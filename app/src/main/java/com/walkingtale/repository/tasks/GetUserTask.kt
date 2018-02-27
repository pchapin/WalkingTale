package com.walkingtale.repository.tasks

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.walkingtale.db.WalkingTaleDb
import com.walkingtale.vo.Resource
import com.walkingtale.vo.Status
import com.walkingtale.vo.User

class GetUserTask(val userId: String, val db: WalkingTaleDb) : AbstractTask<String, User>(userId, db) {

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