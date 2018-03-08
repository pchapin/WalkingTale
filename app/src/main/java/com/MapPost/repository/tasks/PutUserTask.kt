package com.MapPost.repository.tasks

import com.MapPost.db.AppDatabase
import com.MapPost.vo.Resource
import com.MapPost.vo.Status
import com.MapPost.vo.User

class PutUserTask(val user: User, db: AppDatabase) : AbstractTask<User, Void>(user, db) {

    override fun run() {
        dynamoDBMapper.save(user)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}