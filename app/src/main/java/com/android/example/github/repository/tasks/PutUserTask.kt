package com.android.example.github.repository.tasks

import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import com.android.example.github.vo.User

class PutUserTask(val user: User) : AbstractTask<User, Void>(user) {

    override fun run() {
        dynamoDBMapper.save(user)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}