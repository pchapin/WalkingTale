package com.MapPost

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.MapPost.repository.PostRepository
import com.MapPost.vo.Post
import com.MapPost.vo.Resource
import com.MapPost.vo.User


class MainViewModel : ViewModel() {

    private val postRepository = PostRepository

    fun getUser(userId: String): LiveData<Resource<User>> {
        return MutableLiveData()
    }

    fun putUser(user: User): LiveData<Resource<Void>> {
        return MutableLiveData()
    }

    fun getNearbyPosts(): LiveData<Resource<List<Post>>> {
        return postRepository.getNearbyPosts()
    }

    fun putPost(post: Post): LiveData<Resource<Unit>> {
        return postRepository.addPost(post)
    }

}
