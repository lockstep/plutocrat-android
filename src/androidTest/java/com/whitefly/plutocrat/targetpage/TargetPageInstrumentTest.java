package com.whitefly.plutocrat.targetpage;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.SessionManager;
import com.whitefly.plutocrat.mainmenu.events.GetPlutocratEvent;
import com.whitefly.plutocrat.mainmenu.events.LoadTargetsEvent;
import com.whitefly.plutocrat.mainmenu.presenters.MainMenuPresenter;
import com.whitefly.plutocrat.mainmenu.views.ITargetView;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.TargetModel;

import org.junit.Before;
import org.junit.Rule;
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

        AppPreference.createInstance(mContext);
        SessionManager sessionManager = AppPreference.getInstance().getSession();
        sessionManager.access_token = "4kOp4ueTbRtCAx_kIYtmew";
        sessionManager.client = "AemZ1Mi6p9_hUL0Xe9UvzQ";
        sessionManager.expiry = 1466058517L;
        sessionManager.uid = "amy@test.com";
        sessionManager.user_id = 14;
    }

    @Test
    public void testRetrieveFirstPage() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        final int expectedPage = 1;

        MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, null, new ITargetView() {
            @Override
            public void setTargetList(ArrayList<TargetModel> list, MetaModel meta) {
                assertEquals(expectedPage, meta.getInt("current_page"));

                signal.countDown();
            }

            @Override
            public void setPlutocrat(TargetModel user) {

            }
        }, null, null);
        presenter.onLoadTargets(new LoadTargetsEvent(expectedPage, 0));
        signal.await();
    }

    @Test
    public void testRetrieveAssignPage() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        final int expectedPage = 2;

        MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, null, new ITargetView() {
            @Override
            public void setTargetList(ArrayList<TargetModel> list, MetaModel meta) {
                assertEquals(expectedPage, meta.getInt("current_page"));

                signal.countDown();
            }

            @Override
            public void setPlutocrat(TargetModel user) {

            }
        }, null, null);
        presenter.onLoadTargets(new LoadTargetsEvent(expectedPage, 0));
        signal.await();
    }

    @Test
    public void testNoTarget() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        int page = 100;

        MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, null, new ITargetView() {
            @Override
            public void setTargetList(ArrayList<TargetModel> list, MetaModel model) {
                assertEquals(0, list.size());

                signal.countDown();
            }

            @Override
            public void setPlutocrat(TargetModel user) {

            }
        }, null, null);
        presenter.onLoadTargets(new LoadTargetsEvent(page, 0));
        signal.await();
    }

    @Test
    public void testHavePlutocrat() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        TargetModel expectedPlutocrat = new TargetModel();
        AppPreference.getInstance().getSession().savePlutocrat(expectedPlutocrat);

        MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, null, new ITargetView() {
            @Override
            public void setTargetList(ArrayList<TargetModel> list, MetaModel model) {

            }

            @Override
            public void setPlutocrat(TargetModel user) {
                assertNotNull(user);

                signal.countDown();
            }
        }, null, null);
        presenter.onGetPlutocratEvent(new GetPlutocratEvent());
        signal.await();
    }

    @Test
    public void testNoPlutocrat() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        TargetModel expectedPlutocrat = null;
        AppPreference.getInstance().getSession().savePlutocrat(expectedPlutocrat);

        MainMenuPresenter presenter = new MainMenuPresenter(mContext, null, null, new ITargetView() {
            @Override
            public void setTargetList(ArrayList<TargetModel> list, MetaModel model) {

            }

            @Override
            public void setPlutocrat(TargetModel user) {
                assertNull(user);

                signal.countDown();
            }
        }, null, null);
        presenter.onGetPlutocratEvent(new GetPlutocratEvent());
        signal.await();
    }
}
