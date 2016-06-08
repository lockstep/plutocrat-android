package com.whitefly.plutocrat.targetpage;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import com.squareup.otto.Subscribe;
import android.support.test.runner.AndroidJUnit4;

import com.squareup.otto.ThreadEnforcer;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.SessionManager;
import com.whitefly.plutocrat.mainmenu.events.GetPlutocratEvent;
import com.whitefly.plutocrat.mainmenu.events.LoadTargetsEvent;
import com.whitefly.plutocrat.mainmenu.presenters.MainMenuPresenter;
import com.whitefly.plutocrat.mainmenu.views.events.LoadPlutocratCompletedEvent;
import com.whitefly.plutocrat.mainmenu.views.events.LoadTargetCompletedEvent;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.TargetModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import static org.junit.Assert.*;

/**
 * Created by satjapotiamopas on 6/7/16 AD.
 */
@RunWith(AndroidJUnit4.class)
public class TargetPageInstrumentTest {

    private Context mContext;

    @Before
    public void setUp() throws Exception {

        mContext = InstrumentationRegistry.getTargetContext();
        EventBus.getInstance(ThreadEnforcer.ANY);

        AppPreference.createInstance(mContext);
        SessionManager sessionManager = AppPreference.getInstance().getSession();
        sessionManager.access_token = "jr7NG_S1_OIvipy92ftLcg";
        sessionManager.client = "dT7WR_MOEnPyn8hgJcLcwg";
        sessionManager.expiry = 1467707426L;
        sessionManager.uid = "amy@test.com";
        sessionManager.user_id = 14;
    }

    @Test
    public void testRetrieveFirstPage() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        final int expectedPage = 1;

        final MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, null, null);
        Object callback = new Object() {
            @Subscribe
            public void onLoadTargetCompletedEvent(LoadTargetCompletedEvent event) {
                MetaModel meta = event.getMeta();
                assertEquals(expectedPage, meta.getInt("current_page"));

                EventBus.getInstance().unregister(presenter);
                EventBus.getInstance().unregister(this);
                signal.countDown();
            }

            @Subscribe
            public void onLoadPlutocratCompletedEvent(LoadPlutocratCompletedEvent event) {
            }
        };

        EventBus.getInstance().register(presenter);
        EventBus.getInstance().register(callback);
        EventBus.getInstance().post(new LoadTargetsEvent(expectedPage, 0));
        signal.await();
    }

    @Test
    public void testRetrieveAssignPage() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        final int expectedPage = 2;

        final MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, null, null);
        Object callback = new Object() {
            @Subscribe
            public void onLoadTargetCompletedEvent(LoadTargetCompletedEvent event) throws Exception {
                MetaModel meta = event.getMeta();
                assertEquals(expectedPage, meta.getInt("current_page"));

                EventBus.getInstance().unregister(presenter);
                EventBus.getInstance().unregister(this);
                signal.countDown();
            }

            @Subscribe
            public void onLoadPlutocratCompletedEvent(LoadPlutocratCompletedEvent event) throws Exception {
            }
        };

        EventBus.getInstance().register(presenter);
        EventBus.getInstance().register(callback);
        EventBus.getInstance().post(new LoadTargetsEvent(expectedPage, 0));
        signal.await();
    }

    @Test
    public void testNoTarget() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        int page = 100;

        final MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, null, null);
        Object callback = new Object() {
            @Subscribe
            public void onLoadTargetCompletedEvent(LoadTargetCompletedEvent event) {
                MetaModel meta = event.getMeta();
                ArrayList<TargetModel> list = event.getItems();

                assertEquals(0, list.size());

                EventBus.getInstance().unregister(presenter);
                EventBus.getInstance().unregister(this);
                signal.countDown();
            }

            @Subscribe
            public void onLoadPlutocratCompletedEvent(LoadPlutocratCompletedEvent event) {
            }
        };

        EventBus.getInstance().register(presenter);
        EventBus.getInstance().register(callback);
        EventBus.getInstance().post(new LoadTargetsEvent(page, 0));
        signal.await();
    }

    @Test
    public void testHavePlutocrat() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        TargetModel expectedPlutocrat = new TargetModel();
        AppPreference.getInstance().getSession().savePlutocrat(expectedPlutocrat);

        final MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, null, null);
        Object callback = new Object() {
            @Subscribe
            public void onLoadTargetCompletedEvent(LoadTargetCompletedEvent event) {
            }

            @Subscribe
            public void onLoadPlutocratCompletedEvent(LoadPlutocratCompletedEvent event) {
                TargetModel user = event.getPlutocratUser();
                assertNotNull(user);

                EventBus.getInstance().unregister(presenter);
                EventBus.getInstance().unregister(this);
                signal.countDown();
            }
        };

        EventBus.getInstance().register(presenter);
        EventBus.getInstance().register(callback);
        EventBus.getInstance().post(new GetPlutocratEvent());
        signal.await();
    }

    @Test
    public void testNoPlutocrat() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        TargetModel expectedPlutocrat = null;
        AppPreference.getInstance().getSession().savePlutocrat(expectedPlutocrat);

        final MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, null, null);
        Object callback = new Object() {
            @Subscribe
            public void onLoadTargetCompletedEvent(LoadTargetCompletedEvent event) {
            }

            @Subscribe
            public void onLoadPlutocratCompletedEvent(LoadPlutocratCompletedEvent event) {
                TargetModel user = event.getPlutocratUser();
                assertNull(user);

                EventBus.getInstance().unregister(presenter);
                EventBus.getInstance().unregister(this);
                signal.countDown();
            }
        };

        EventBus.getInstance().register(presenter);
        EventBus.getInstance().register(callback);
        EventBus.getInstance().post(new GetPlutocratEvent());
        signal.await();
    }
}
