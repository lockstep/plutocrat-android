package com.whitefly.plutocrat.homepage;

import android.content.Context;

import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.mainmenu.events.SetHomeStateEvent;
import com.whitefly.plutocrat.mainmenu.events.UpdateUserNoticeIdEvent;
import com.whitefly.plutocrat.mainmenu.fragments.HomeFragment;
import com.whitefly.plutocrat.mainmenu.presenters.MainMenuPresenter;
import com.whitefly.plutocrat.mainmenu.views.IHomeView;
import com.whitefly.plutocrat.models.UserModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by satjapotiamopas on 6/2/16 AD.
 */
@RunWith(MockitoJUnitRunner.class)
public class HomePageUnitTest {

    @Mock
    private Context mContext;

    @Before
    public void setupAppPreference() {
        AppPreference.createInstance(mContext);
    }

    @Test
    public void testActiveStateWithFirstTime() throws Exception {
        UserModel model = new UserModel();
        model.defeatedAt = null;
        model.isUnderBuyoutThreat = false;
        model.userNoticeId = UserModel.NOTICE_GETTING_STARTED;
        AppPreference.getInstance().getSession().updateActiveUser(model);

        MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, new IHomeView() {
            @Override
            public void changeState(HomeFragment.State state, int noticeId) {
                assertEquals(HomeFragment.State.Default, state);
                assertEquals(UserModel.NOTICE_GETTING_STARTED, noticeId);
            }

            @Override
            public void handleNotificationEnable(boolean isEnabled) {

            }
        });
        presenter.onSetHomeState(new SetHomeStateEvent());
    }

    @Test
    public void testActiveStateWithEnableNotification() throws Exception {
        UserModel model = new UserModel();
        model.defeatedAt = null;
        model.isUnderBuyoutThreat = false;
        model.userNoticeId = UserModel.NOTICE_ENABLE_PUSH_NOTIFICATION;
        AppPreference.getInstance().getSession().updateActiveUser(model);

        MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, new IHomeView() {
            @Override
            public void changeState(HomeFragment.State state, int noticeId) {
                assertEquals(HomeFragment.State.Default, state);
                assertEquals(UserModel.NOTICE_ENABLE_PUSH_NOTIFICATION, noticeId);
            }

            @Override
            public void handleNotificationEnable(boolean isEnabled) {

            }
        });
        presenter.onSetHomeState(new SetHomeStateEvent());
    }

    @Test
    public void testActiveState() throws Exception {
        UserModel model = new UserModel();
        model.defeatedAt = null;
        model.isUnderBuyoutThreat = false;
        model.userNoticeId = UserModel.NOTICE_DEFAULT;
        AppPreference.getInstance().getSession().updateActiveUser(model);

        MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, new IHomeView() {
            @Override
            public void changeState(HomeFragment.State state, int noticeId) {
                assertEquals(HomeFragment.State.Default, state);
                assertEquals(UserModel.NOTICE_DEFAULT, noticeId);
            }

            @Override
            public void handleNotificationEnable(boolean isEnabled) {

            }
        });
        presenter.onSetHomeState(new SetHomeStateEvent());
    }

    @Test
    public void testUpdateUserNoticeId() throws Exception {
        UserModel model = new UserModel();
        model.defeatedAt = null;
        model.isUnderBuyoutThreat = false;
        model.userNoticeId = UserModel.NOTICE_GETTING_STARTED;
        AppPreference.getInstance().getSession().updateActiveUser(model);

        final int expectedNoticeId = UserModel.NOTICE_ENABLE_PUSH_NOTIFICATION;

        MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, new IHomeView() {
            @Override
            public void changeState(HomeFragment.State state, int noticeId) {
                assertEquals(HomeFragment.State.Default, state);
                assertEquals(expectedNoticeId, noticeId);
            }

            @Override
            public void handleNotificationEnable(boolean isEnabled) {

            }
        });
        presenter.onUpdateUserNoticeId(new UpdateUserNoticeIdEvent(expectedNoticeId));
    }

    @Test
    public void testAttackedState() {
        UserModel model = new UserModel();
        model.defeatedAt = null;
        model.isUnderBuyoutThreat = true;
        AppPreference.getInstance().getSession().updateActiveUser(model);

        MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, new IHomeView() {
            @Override
            public void changeState(HomeFragment.State state, int noticeId) {
                assertEquals(HomeFragment.State.Threat, state);
            }

            @Override
            public void handleNotificationEnable(boolean isEnabled) {

            }
        });
        presenter.onSetHomeState(new SetHomeStateEvent());
    }

    @Test
    public void testSuspendedUser() {
        UserModel model = new UserModel();
        model.defeatedAt = new Date();
        model.isUnderBuyoutThreat = false;
        AppPreference.getInstance().getSession().updateActiveUser(model);

        MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, new IHomeView() {
            @Override
            public void changeState(HomeFragment.State state, int noticeId) {
                assertEquals(HomeFragment.State.Suspend, state);
            }

            @Override
            public void handleNotificationEnable(boolean isEnabled) {

            }
        });
        presenter.onSetHomeState(new SetHomeStateEvent());
    }
}
