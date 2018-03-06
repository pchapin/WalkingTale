package com.WalkingTale.repository.tasks

import com.WalkingTale.db.WalkingTaleDb
import com.WalkingTale.vo.Resource
import com.WalkingTale.vo.Status
import com.WalkingTale.vo.User

class PutUserTask(val user: User, db: WalkingTaleDb) : AbstractTask<User, Void>(user, db) {

    override fun run() {
        dynamoDBMapper.save(user)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}