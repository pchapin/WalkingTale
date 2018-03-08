package com.MapPost.ui.play;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.MapPost.repository.PostRepository;
import com.MapPost.repository.UserRepository;
import com.MapPost.util.AbsentLiveData;
import com.MapPost.vo.Post;
import com.MapPost.vo.Resource;

import java.util.List;

import javax.inject.Inject;

public class PlayViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();

    @Inject
    PlayViewModel(PostRepository repository, UserRepository userRepository) {

    }

    LiveData<Resource<List<Post>>> getPosts() {
        return AbsentLiveData.create();
    }
}
