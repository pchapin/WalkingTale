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
    var usersPosts: LiveData<Resource<List<Post>>> = MutableLiveData()
    private var postBounds: MutableLiveData<PostRepository.CornerLatLng> = MutableLiveData()
    private var currentUserId: MutableLiveData<String> = MutableLiveData()

    init {
        localPosts = Transformations.switchMap(postBounds, Function {
            return@Function postRepository.getNearbyPosts(it)
        })
        currentUser = Transformations.switchMap(currentUserId, Function {
            return@Function userRepository.loadUser(it)
        })
        usersPosts = Transformations.switchMap(currentUserId, Function {
            return@Function postRepository.getPostsForUser(it)
        })
    }

    fun setCurrentUserId(userId: String) {
        this.currentUserId.value = userId
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

    fun deleteUserS3Content(context: Context, user: User): LiveData<Resource<Unit>> {
        return postRepository.deleteUserS3Content(context, user)
    }

    fun deleteUsersPosts(user: User, posts: List<Post>): LiveData<Resource<Unit>> {
        return postRepository.deleteUsersPosts(user, posts)
    }

    fun deleteUser(user: User): LiveData<Resource<Unit>> {
        return userRepository.deleteUser(user)
    }
}
