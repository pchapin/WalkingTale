package com.walkingtale.ui.play;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.VisibleForTesting;

import com.walkingtale.repository.StoryRepository;
import com.walkingtale.repository.tasks.StoryKey;
import com.walkingtale.vo.Resource;
import com.walkingtale.vo.Story;
import com.walkingtale.walkingTale.Chapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class PlayViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private final StoryRepository storyRepository;
    @VisibleForTesting
    LiveData<List<Chapter>> availableChapters = new MutableLiveData<>();
    private Story story;
    private MutableLiveData<Chapter> currentChapter = new MutableLiveData<>();
    private MutableLiveData<Chapter> nextChapter = new MutableLiveData<>();
    private LiveData<Boolean> isCurrentFinal = new MutableLiveData<>();

    @Inject
    PlayViewModel(StoryRepository repository) {
        storyRepository = repository;

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

    public LiveData<Resource<Story>> getStory(StoryKey storyKey) {
        return storyRepository.getOneStory(storyKey);
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
