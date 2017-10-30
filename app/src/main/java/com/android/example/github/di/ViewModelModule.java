package com.android.example.github.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.android.example.github.ui.expositionviewer.ExpositionViewerViewModel;
import com.android.example.github.ui.repo.RepoViewModel;
import com.android.example.github.ui.search.SearchViewModel;
import com.android.example.github.ui.storycreate.StoryCreateViewModel;
import com.android.example.github.ui.storyfeed.StoryFeedViewModel;
import com.android.example.github.ui.storyreader.StoryPlayViewModel;
import com.android.example.github.ui.user.UserViewModel;
import com.android.example.github.viewmodel.GithubViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(UserViewModel.class)
    abstract ViewModel bindUserViewModel(UserViewModel userViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel.class)
    abstract ViewModel bindSearchViewModel(SearchViewModel searchViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(RepoViewModel.class)
    abstract ViewModel bindRepoViewModel(RepoViewModel repoViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StoryCreateViewModel.class)
    abstract ViewModel bindStoryCreateViewModel(StoryCreateViewModel storyCreateViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StoryFeedViewModel.class)
    abstract ViewModel bindStoryFeedViewModel(StoryFeedViewModel storyFeedViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StoryPlayViewModel.class)
    abstract ViewModel bindStoryPlayViewModel(StoryPlayViewModel storyPlayViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ExpositionViewerViewModel.class)
    abstract ViewModel bindExpositionViewerViewModel(ExpositionViewerViewModel expositionViewerViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(GithubViewModelFactory factory);
}
