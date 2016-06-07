package com.whitefly.plutocrat.homepage;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.action.ViewActions.*;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.whitefly.plutocrat.MainActivity;
import com.whitefly.plutocrat.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by satjapotiamopas on 6/2/16 AD.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomePageAutomateTest {
    private static final int SPLASH_SCREEN_WAITING_TIME = 5000;
    private static final String TEST_USER_EMAIL = "amy@test.com";
    private static final String TEST_USER_PASSWORD = "test1234";

    public void gotToHomePage() throws Exception {
        Thread.sleep(SPLASH_SCREEN_WAITING_TIME);

        try {
            ViewInteraction loginView =  onView(withId(R.id.btn_login_link));
            loginView.check(matches(withText(R.string.caption_login)));
            loginView.perform(click());

            onView(withId(R.id.edt_signin_email)).perform(typeText(TEST_USER_EMAIL), closeSoftKeyboard());
            onView(withId(R.id.edt_signin_pw)).perform(typeText(TEST_USER_PASSWORD), closeSoftKeyboard());
            onView(withId(R.id.btn_signin)).perform(click());

        } catch (NoMatchingViewException ex) {
            // We're in home page
        }
    }

    @Rule
    public ActivityTestRule<MainActivity> mMainMenuActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testActiveUserWithFirstTime() throws Exception {
        gotToHomePage();
        onView(withId(R.id.tv_home_title))
                .check(matches(withText(R.string.caption_survival_time)));
    }

    @Test
    public void testAttackedUser() throws Exception {
        // Expect: Correct top bar
        gotToHomePage();
        onView(withId(R.id.tv_home_title))
                .check(matches(withText(R.string.caption_active_threat)));
    }

    @Test
    public void testSuspendUser() throws Exception {
        // Expect: Correct top bar
        gotToHomePage();
        onView(withId(R.id.tv_home_title))
                .check(matches(withText(R.string.caption_you_survived)));
    }
}
