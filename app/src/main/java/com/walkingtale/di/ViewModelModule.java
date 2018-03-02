package com.walkingtale.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.walkingtale.MainViewModel;
import com.walkingtale.ui.create.CreateViewModel;
import com.walkingtale.ui.feed.FeedViewModel;
import com.walkingtale.ui.overview.OverviewViewModel;
import com.walkingtale.ui.play.PlayViewModel;
import com.walkingtale.ui.profile.ProfileViewModel;
import com.walkingtale.ui.search.SearchViewModel;
import com.walkingtale.viewmodel.GithubViewModelFactory;

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
    @ViewModelKey(CreateViewModel.class)
    abstract ViewModel bindStoryCreateViewModel(CreateViewModel createViewModel);

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
