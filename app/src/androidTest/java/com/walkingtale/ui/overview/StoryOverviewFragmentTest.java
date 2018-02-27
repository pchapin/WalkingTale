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

package com.walkingtale.ui.overview;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.StringRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.walkingtale.R;
import com.walkingtale.binding.FragmentBindingAdapters;
import com.walkingtale.testing.SingleFragmentActivity;
import com.walkingtale.ui.common.NavigationController;
import com.walkingtale.util.TaskExecutorWithIdlingResourceRule;
import com.walkingtale.util.TestUtil;
import com.walkingtale.vo.Resource;
import com.walkingtale.vo.Story;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

@RunWith(AndroidJUnit4.class)
public class StoryOverviewFragmentTest {
    @Rule
    public ActivityTestRule<SingleFragmentActivity> activityRule =
            new ActivityTestRule<>(SingleFragmentActivity.class, true, true);
    @Rule
    public TaskExecutorWithIdlingResourceRule executorRule =
            new TaskExecutorWithIdlingResourceRule();
    private MutableLiveData<Resource<Story>> repo = new MutableLiveData<>();
    private OverviewFragment repoFragment;
    private OverviewViewModel viewModel;

    private FragmentBindingAdapters fragmentBindingAdapters;
    private NavigationController navigationController;


    @Before
    public void init() {
//        repoFragment = OverviewFragment.create(-1);
//        viewModel = mock(OverviewViewModel.class);
//        fragmentBindingAdapters = mock(FragmentBindingAdapters.class);
//        navigationController = mock(NavigationController.class);
//
////        when(viewModel.getStory()).thenReturn(repo);
////        when(viewModel.getContributors()).thenReturn(contributors);
//
//        repoFragment.viewModelFactory = ViewModelUtil.createFor(viewModel);
////        repoFragment.dataBindingComponent = () -> fragmentBindingAdapters;
//        repoFragment.navigationController = navigationController;
////
//        activityRule.getActivity().setFragment(repoFragment);
    }

    @Test
    public void testLoading() {
        repo.postValue(Resource.loading(null));
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()));
        onView(withId(R.id.retry)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testValueWhileLoading() {
        Story story = TestUtil.createRepo("yigit", "foo", "foo-bar");
        this.repo.postValue(Resource.loading(story));
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
        onView(withId(R.id.name)).check(matches(
                withText(getString(R.string.repo_full_name, "yigit", "foo"))));
        onView(withId(R.id.description)).check(matches(withText("foo-bar")));
    }

    @Test
    public void testLoaded() throws InterruptedException {
        Story story = TestUtil.createRepo("foo", "bar", "buzz");
        this.repo.postValue(Resource.success(story));
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
        onView(withId(R.id.name)).check(matches(
                withText(getString(R.string.repo_full_name, "foo", "bar"))));
        onView(withId(R.id.description)).check(matches(withText("buzz")));
    }

    @Test
    public void testError() throws InterruptedException {
        repo.postValue(Resource.error("foo", null));
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
        onView(withId(R.id.retry)).check(matches(isDisplayed()));
        onView(withId(R.id.retry)).perform(click());
//        verify(viewModel).retry();
        repo.postValue(Resource.loading(null));

        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()));
        onView(withId(R.id.retry)).check(matches(not(isDisplayed())));
        Story story = TestUtil.createRepo("owner", "name", "desc");
        this.repo.postValue(Resource.success(story));

        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
        onView(withId(R.id.retry)).check(matches(not(isDisplayed())));
        onView(withId(R.id.name)).check(matches(
                withText(getString(R.string.repo_full_name, "owner", "name"))));
        onView(withId(R.id.description)).check(matches(withText("desc")));
    }

    @Test
    public void testContributors() {
        setContributors("aa", "bb");
//        onView(listMatcher().atPosition(0))
//                .check(matches(hasDescendant(withText("aa"))));
//        onView(listMatcher().atPosition(1))
//                .check(matches(hasDescendant(withText("bb"))));
    }

//    @NonNull
//    private RecyclerViewMatcher listMatcher() {
//        return new RecyclerViewMatcher(R.id.contributor_list);
//    }

    @Test
    public void testContributorClick() {
        setContributors("aa", "bb", "cc");
        onView(withText("cc")).perform(click());
//        verify(navigationController).navigateToUser("cc");
    }

    @Test
    public void nullRepo() {
        this.repo.postValue(null);
        onView(withId(R.id.name)).check(matches(not(isDisplayed())));
    }

    @Test
    public void nullContributors() {
//        setContributors("a", "b", "c");
//        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("a"))));
//        contributors.postValue(null);
//        onView(listMatcher().atPosition(0)).check(doesNotExist());
    }

    private void setContributors(String... names) {
//        Story repo = TestUtil.createRepo("foo", "bar", "desc");
//        List<Contributor> contributors = new ArrayList<>();
//        int contributionCount = 100;
//        for (String name : names) {
//            contributors.add(TestUtil.createContributor(repo, name, contributionCount--));
//        }
//        this.contributors.postValue(Resource.success(contributors));
    }

    private String getString(@StringRes int id, Object... args) {
        return InstrumentationRegistry.getTargetContext().getString(id, args);
    }
}