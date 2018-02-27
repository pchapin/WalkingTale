package com.walkingtale.repository.tasks

import com.walkingtale.db.GithubDb
import com.walkingtale.vo.Resource
import com.walkingtale.vo.Status
import com.walkingtale.vo.User

class PutUserTask(val user: User, db: GithubDb) : AbstractTask<User, Void>(user, db) {

    override fun run() {
        dynamoDBMapper.save(user)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}