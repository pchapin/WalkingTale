package com.MapPost

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel

import com.MapPost.repository.PostRepository
import com.MapPost.repository.UserRepository
import com.MapPost.vo.Resource
import com.MapPost.vo.User


class MainViewModel
internal constructor(private val postRepository: PostRepository, private val userRepository: UserRepository) : ViewModel() {

    internal fun getUser(userId: String): LiveData<Resource<User>> {
        return userRepository.loadUser(userId)
    }

    internal fun createUser(user: User): LiveData<Resource<Void>> {
        return userRepository.putUser(user)
    }
}
