package com.walkingtale;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.walkingtale.repository.StoryRepository;
import com.walkingtale.repository.UserRepository;
import com.walkingtale.vo.Resource;
import com.walkingtale.vo.User;

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
