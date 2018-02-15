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
    private UserRepository userRepository;

    @Inject
    MainViewModel(RepoRepository repository, UserRepository userRepository) {
        this.userRepository = userRepository;
        newUser = Transformations.switchMap(createNewUser, userRepository::putUser);
    }

    LiveData<Resource<User>> getUser(String userId) {
        return userRepository.loadUser(userId);
    }

    void setUserId(@NonNull String id) {
        userId.setValue(id);
    }

    void setCreateNewUser(@NonNull User user) {
        createNewUser.setValue(user);
    }
}
