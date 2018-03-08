package com.MapPost.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.MapPost.MainViewModel;
import com.MapPost.ui.feed.FeedViewModel;
import com.MapPost.ui.overview.OverviewViewModel;
import com.MapPost.ui.play.PlayViewModel;
import com.MapPost.ui.profile.ProfileViewModel;
import com.MapPost.ui.search.SearchViewModel;
import com.MapPost.viewmodel.GithubViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(OverviewViewModel.class)
    abstract ViewModel bindRepoViewModel(OverviewViewModel overviewViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(FeedViewModel.class)
    abstract ViewModel bindStoryFeedViewModel(FeedViewModel feedViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PlayViewModel.class)
    abstract ViewModel bindStoryPlayViewModel(PlayViewModel playViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel.class)
    abstract ViewModel bindSearchViewModel(SearchViewModel searchViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel.class)
    abstract ViewModel bindProfileViewModel(ProfileViewModel profileViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel.class)
    abstract ViewModel bindMainViewModel(MainViewModel mainViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(GithubViewModelFactory factory);
}
