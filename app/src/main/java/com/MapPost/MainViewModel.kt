package com.MapPost

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.MapPost.repository.PostRepository
import com.MapPost.repository.UserRepository
import com.MapPost.vo.Post
import com.MapPost.vo.Resource
import com.MapPost.vo.User


class MainViewModel : ViewModel() {

    private val postRepository = PostRepository
    private val userRepository = UserRepository
    var currentUser: User? = null

    fun getUser(userId: String): LiveData<Resource<User>> {
        return userRepository.loadUser(userId)
    }

    fun putUser(user: User): LiveData<Resource<Unit>> {
        return userRepository.putUser(user)
    }

    fun getNearbyPosts(): LiveData<Resource<List<Post>>> {
        return postRepository.getNearbyPosts()
    }

    fun putPost(post: Post): LiveData<Resource<Unit>> {
        return postRepository.addPost(post)
    }

}
