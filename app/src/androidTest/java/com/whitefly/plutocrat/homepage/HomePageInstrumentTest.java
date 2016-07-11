package com.whitefly.plutocrat.homepage;

import android.app.Application;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.Espresso.*;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.AndroidJUnitRunner;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.SessionManager;
import com.whitefly.plutocrat.models.UserModel;
import com.whitefly.plutocrat.splash.events.LoadUserDataEvent;
import com.whitefly.plutocrat.splash.presenters.SplashPresenter;
import com.whitefly.plutocrat.splash.views.ISplashView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import static org.junit.Assert.*;

/**
 * Created by satjapotiamopas on 6/2/16 AD.
 */
@RunWith(AndroidJUnit4.class)
public class HomePageInstrumentTest {

    private Context mContext;

    @Before
    public void setUp() throws Exception {

        mContext = InstrumentationRegistry.getTargetContext();
        AppPreference.createInstance(mContext);
        SessionManager sessionManager = AppPreference.getInstance().getSession();
        sessionManager.access_token = "4kOp4ueTbRtCAx_kIYtmew";
        sessionManager.client = "AemZ1Mi6p9_hUL0Xe9UvzQ";
        sessionManager.expiry = 1466058517L;
        sessionManager.uid = "amy@test.com";
    }

    @Test
    public void testConnectLoadUserDataAPI() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        final int expectedUserId = 14;

        AppPreference.getInstance().getSession().user_id = expectedUserId;
        SplashPresenter presenter = new SplashPresenter(mContext, new ISplashView() {
            @Override
            public void loadActivity(boolean isLogin) {
                signal.countDown();

                UserModel model = AppPreference.getInstance().getSession().getActiveUser();
                assertNotNull(model);
                assertEquals(model.id, expectedUserId);
            }
        });
        presenter.onLoadingUserData(new LoadUserDataEvent());
        signal.await();
    }
}
