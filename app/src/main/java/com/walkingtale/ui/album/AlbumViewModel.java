/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.walkingtale.ui.album;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.VisibleForTesting;

import com.walkingtale.repository.StoryRepository;
import com.walkingtale.util.AbsentLiveData;
import com.walkingtale.vo.Resource;
import com.walkingtale.vo.Story;

import javax.inject.Inject;

public class AlbumViewModel extends ViewModel {
    @VisibleForTesting
    private final MutableLiveData<String> repoId;
    private final LiveData<Resource<Story>> repo;

    @Inject
    public AlbumViewModel(StoryRepository repository) {
        this.repoId = new MutableLiveData<>();
        repo = Transformations.switchMap(repoId, input -> {
            if (input == null) {
                return AbsentLiveData.create();
            }
            return repository.getOneStory(null);
        });
    }

    public LiveData<Resource<Story>> getRepo() {
        return repo;
    }

    void setId(String id) {
        repoId.setValue(id);
    }
}