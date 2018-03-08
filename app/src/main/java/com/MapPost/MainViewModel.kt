package com.MapPost

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.MapPost.vo.Resource
import com.MapPost.vo.User


class MainViewModel : ViewModel() {

    fun getUser(userId: String): LiveData<Resource<User>> {
        return MutableLiveData()
    }

    fun createUser(user: User): LiveData<Resource<Void>> {
        return MutableLiveData()
    }
}
