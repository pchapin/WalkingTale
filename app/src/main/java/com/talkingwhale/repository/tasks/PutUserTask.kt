package com.talkingwhale.repository.tasks

import com.talkingwhale.db.AppDatabase
import com.talkingwhale.vo.Resource
import com.talkingwhale.vo.Status
import com.talkingwhale.vo.User

class PutUserTask(val user: User, db: AppDatabase) : AbstractTask<User, Void>(user) {

    override fun run() {
        dynamoDBMapper.save(user)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}