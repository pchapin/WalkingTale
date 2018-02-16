///*
// * Copyright (C) 2017 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.android.example.github.repository;
//
//import android.arch.core.executor.testing.InstantTaskExecutorRule;
//import android.arch.lifecycle.LiveData;
//import android.arch.lifecycle.MutableLiveData;
//import android.arch.lifecycle.Observer;
//
//import com.android.example.github.api.ApiResponse;
//import com.android.example.github.api.GithubService;
//import com.android.example.github.db.GithubDb;
//import com.android.example.github.db.StoryDao;
//import com.android.example.github.util.InstantAppExecutors;
//import com.android.example.github.util.TestUtil;
//import com.android.example.github.vo.Resource;
//import com.android.example.github.vo.Story;
//
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.JUnit4;
//
//import java.io.IOException;
//
//import static com.android.example.github.util.ApiUtil.successCall;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoMoreInteractions;
//import static org.mockito.Mockito.when;
//
//@SuppressWarnings("unchecked")
//@RunWith(JUnit4.class)
//public class StoryRepositoryTest {
//    @Rule
//    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();
//    private StoryRepository repository;
//    private StoryDao dao;
//    private GithubService service;
//
//    @Before
//    public void init() {
//        dao = mock(StoryDao.class);
//        service = mock(GithubService.class);
//        GithubDb db = mock(GithubDb.class);
//        when(db.repoDao()).thenReturn(dao);
//        repository = new StoryRepository(new InstantAppExecutors(), db, dao, service);
//    }
//
//    @Test
//    public void loadRepoFromNetwork() throws IOException {
//        MutableLiveData<Story> dbData = new MutableLiveData<>();
////        when(dao.load("foo", "bar")).thenReturn(dbData);
//
//        Story story = TestUtil.createRepo("foo", "bar", "desc");
//        LiveData<ApiResponse<Story>> call = successCall(story);
//        when(service.getRepo("foo")).thenReturn(call);
//
////        LiveData<Resource<Story>> data = repository.loadRepo("foo", "bar");
////        verify(dao).load("foo", "bar");
//        verifyNoMoreInteractions(service);
//
//        Observer observer = mock(Observer.class);
////        data.observeForever(observer);
//        verifyNoMoreInteractions(service);
//        verify(observer).onChanged(Resource.loading(null));
//        MutableLiveData<Story> updatedDbData = new MutableLiveData<>();
////        when(dao.load("foo", "bar")).thenReturn(updatedDbData);
//
//        dbData.postValue(null);
//        verify(service).getRepo("foo");
//        verify(dao).insert(story);
//
//        updatedDbData.postValue(story);
//        verify(observer).onChanged(Resource.success(story));
//    }
//
//    @Test
//    public void searchNextPage_null() {
//        when(dao.findSearchResult("foo")).thenReturn(null);
//        Observer<Resource<Boolean>> observer = mock(Observer.class);
//        repository.searchNextPage("foo").observeForever(observer);
//        verify(observer).onChanged(null);
//    }
//}