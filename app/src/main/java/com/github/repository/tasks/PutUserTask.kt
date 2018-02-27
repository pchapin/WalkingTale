package com.github.repository.tasks

import com.github.db.GithubDb
import com.github.vo.Resource
import com.github.vo.Status
import com.github.vo.User

class PutUserTask(val user: User, db: GithubDb) : AbstractTask<User, Void>(user, db) {

    override fun run() {
        dynamoDBMapper.save(user)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}