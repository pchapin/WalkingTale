package com.MapPost;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.MapPost.repository.StoryRepository;
import com.MapPost.repository.UserRepository;
import com.MapPost.vo.Resource;
import com.MapPost.vo.User;

import javax.inject.Inject;

public class MainViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final StoryRepository storyRepository;

    @Inject
    MainViewModel(StoryRepository repository, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.storyRepository = repository;
    }

    LiveData<Resource<User>> getUser(String userId) {
        return userRepository.loadUser(userId);
    }

    LiveData<Resource<Void>> createUser(User user) {
        return userRepository.putUser(user);
    }
}
