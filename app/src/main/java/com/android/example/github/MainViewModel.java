package com.android.example.github;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.android.example.github.repository.RepoRepository;
import com.android.example.github.repository.UserRepository;
import com.android.example.github.vo.Resource;
import com.android.example.github.vo.User;

import javax.inject.Inject;

public class MainViewModel extends ViewModel {

    LiveData<Resource<User>> user = new MutableLiveData<>();
    LiveData<Resource<User>> newUser = new MutableLiveData<>();
    private MutableLiveData<String> userId = new MutableLiveData<>();
    private MutableLiveData<User> createNewUser = new MutableLiveData<>();

    @Inject
    MainViewModel(RepoRepository repository, UserRepository userRepository) {
        user = Transformations.switchMap(userId, userRepository::loadUser);
        newUser = Transformations.switchMap(createNewUser, userRepository::putUser);
    }

    void setUserId(@NonNull String id) {
        userId.setValue(id);
    }

    void setCreateNewUser(@NonNull User user) {
        createNewUser.setValue(user);
    }
}
