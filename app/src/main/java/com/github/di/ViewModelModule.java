package com.github.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.github.MainViewModel;
import com.github.ui.album.AlbumViewModel;
import com.github.ui.create.CreateViewModel;
import com.github.ui.feed.FeedViewModel;
import com.github.ui.overview.OverviewViewModel;
import com.github.ui.play.PlayViewModel;
import com.github.ui.profile.ProfileViewModel;
import com.github.ui.search.SearchViewModel;
import com.github.viewmodel.GithubViewModelFactory;

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
    @ViewModelKey(AlbumViewModel.class)
    abstract ViewModel bindExpositionViewerViewModel(AlbumViewModel albumViewModel);

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
