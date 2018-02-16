package com.android.example.github.ui.play;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.VisibleForTesting;

import com.android.example.github.repository.StoryRepository;
import com.android.example.github.vo.Resource;
import com.android.example.github.vo.Story;
import com.android.example.github.walkingTale.Chapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class PlayViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    @VisibleForTesting
    private final MutableLiveData<String> repoId;
    private final LiveData<Resource<Story>> repo;
    LiveData<List<Chapter>> availableChapters = new MutableLiveData<>();
    private Story story;
    private MutableLiveData<Chapter> currentChapter = new MutableLiveData<>();
    private MutableLiveData<Chapter> nextChapter = new MutableLiveData<>();
    private LiveData<Boolean> isCurrentFinal = new MutableLiveData<>();


    @Inject
    PlayViewModel(StoryRepository repository) {
        this.repoId = new MutableLiveData<>();
        repo = Transformations.switchMap(repoId, repository::loadRepo);

        availableChapters = Transformations.map(currentChapter, (Chapter current) -> {
            if (current == null) return Collections.emptyList();

            List<Chapter> chapterList = new ArrayList<>();
            for (Chapter chapter : story.chapters) {
                if (chapter.getId() <= current.getId()) {
                    chapterList.add(chapter);
                }
            }
            return chapterList;
        });

        isCurrentFinal = Transformations.map(availableChapters, input -> input.size() == story.chapters.size());
    }

    public LiveData<Resource<Story>> getRepo() {
        return repo;
    }

    void setId(String id) {
        repoId.setValue(id);
    }

    LiveData<Chapter> getCurrentChapter() {
        return currentChapter;
    }

    LiveData<Chapter> getNextChapter() {
        return nextChapter;
    }

    void setStory(Story story) throws IllegalArgumentException {
        if (this.story != null)
            throw new IllegalArgumentException("Story has already been initialized");
        this.story = story;
        currentChapter.setValue(this.story.chapters.get(0));
        nextChapter.setValue(this.story.chapters.get(1));
    }

    boolean incrementChapter() {
        if (currentChapter.getValue().getId() == story.chapters.size() - 1) {
            return false;
        } else if (nextChapter.getValue().getId() + 1 == story.chapters.size()) {
            // Next is null to show there is no next chapter after the final one
            currentChapter.setValue(story.chapters.get(currentChapter.getValue().getId() + 1));
            nextChapter.setValue(null);
        } else {
            currentChapter.setValue(story.chapters.get(currentChapter.getValue().getId() + 1));
            nextChapter.setValue(story.chapters.get(nextChapter.getValue().getId() + 1));
        }
        return true;
    }

    boolean isStorySet() {
        return story != null;
    }

    public LiveData<Boolean> getIsCurrentFinal() {
        return isCurrentFinal;
    }
}
