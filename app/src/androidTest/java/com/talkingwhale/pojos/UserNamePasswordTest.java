package com.talkingwhale.pojos;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.talkingwhale.R;
import com.talkingwhale.activities.AuthenticatorActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class UserNamePasswordTest {

    @Rule
    public ActivityTestRule<AuthenticatorActivity> mActivityTestRule = new ActivityTestRule<>(AuthenticatorActivity.class);

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Test
    public void authenticatorActivityTest() {
        ViewInteraction editText = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withClassName(is("com.amazonaws.mobile.auth.userpools.FormView")),
                                0),
                        1),
                        isDisplayed()));
        editText.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html


        ViewInteraction editText2 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withClassName(is("com.amazonaws.mobile.auth.userpools.FormView")),
                                0),
                        1),
                        isDisplayed()));
        editText2.perform(replaceText("hell"), closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction editText3 = onView(
                allOf(withText("hell"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.amazonaws.mobile.auth.userpools.FormView")),
                                        0),
                                1),
                        isDisplayed()));
        editText3.perform(replaceText("hello"));

        ViewInteraction editText4 = onView(
                allOf(withText("hello"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.amazonaws.mobile.auth.userpools.FormView")),
                                        0),
                                1),
                        isDisplayed()));
        editText4.perform(closeSoftKeyboard());

        ViewInteraction editText5 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withClassName(is("com.amazonaws.mobile.auth.userpools.FormEditText")),
                                1),
                        0),
                        isDisplayed()));
        editText5.perform(replaceText("world"), closeSoftKeyboard());

        ViewInteraction editText6 = onView(
                allOf(withText("world"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.amazonaws.mobile.auth.userpools.FormEditText")),
                                        1),
                                0),
                        isDisplayed()));
        editText6.perform(pressImeActionButton());

        ViewInteraction button = onView(
                allOf(withText("Sign In"),
                        childAtPosition(
                                allOf(withId(R.id.user_pool_sign_in_view_id),
                                        childAtPosition(
                                                withClassName(is("com.amazonaws.mobile.auth.ui.SignInView")),
                                                1)),
                                1),
                        isDisplayed()));
        button.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatButton = onView(
                allOf(withId(android.R.id.button3), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                0)));
        appCompatButton.perform(scrollTo(), click());

    }
}
