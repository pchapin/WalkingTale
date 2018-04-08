package com.talkingwhale.activities

import android.arch.core.util.Function
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.talkingwhale.pojos.*
import com.talkingwhale.repository.PostGroupRepository
import com.talkingwhale.repository.PostRepository
import com.talkingwhale.repository.UserRepository


class MainViewModel : ViewModel() {

    private val postRepository = PostRepository
    private val userRepository = UserRepository
    private val postGroupRepository = PostGroupRepository
    var currentUser: LiveData<Resource<User>> = MutableLiveData()
    var localPosts: LiveData<Resource<List<Post>>> = MutableLiveData()
    private var postBounds: MutableLiveData<PostRepository.CornerLatLng> = MutableLiveData()
    private var userId: MutableLiveData<String> = MutableLiveData()

    init {
        localPosts = Transformations.switchMap(postBounds, Function {
            return@Function postRepository.getNearbyPosts(it)
        })
        currentUser = Transformations.switchMap(userId, Function {
            return@Function userRepository.loadUser(it)
        })
    }

    fun setUserId(userId: String) {
        this.userId.value = userId
    }

    fun putUser(user: User): LiveData<Resource<Unit>> {
        return userRepository.putUser(user)
    }

    fun putPost(post: Post): LiveData<Resource<Unit>> {
        return postRepository.addPost(post)
    }

    fun putPosts(posts: List<Post>): LiveData<Resource<List<DynamoDBMapper.FailedBatch>>> {
        return postRepository.putPosts(posts)
    }

    fun putFile(pair: Pair<Post, Context>): LiveData<Resource<Post>> {
        if (pair.first.type == PostType.TEXT) {
            val result: MutableLiveData<Resource<Post>> = MutableLiveData()
            result.value = Resource(Status.SUCCESS, pair.first, "")
            return result
        }
        return postRepository.putFile(pair)
    }

    fun setPostBounds(cornerLatLng: PostRepository.CornerLatLng) {
        postBounds.value = cornerLatLng
    }

    fun deletePost(post: Post): LiveData<Resource<Unit>> {
        return postRepository.deletePost(post)
    }

    fun putPostGroup(postGroup: PostGroup): LiveData<Resource<Unit>> {
        return postGroupRepository.putPostGroup(postGroup)
    }
}
