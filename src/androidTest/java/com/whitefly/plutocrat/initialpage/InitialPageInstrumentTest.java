package com.whitefly.plutocrat.initialpage;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.SessionManager;
import com.whitefly.plutocrat.mainmenu.events.EngageClickEvent;
import com.whitefly.plutocrat.mainmenu.events.ExecuteShareEvent;
import com.whitefly.plutocrat.mainmenu.presenters.MainMenuPresenter;
import com.whitefly.plutocrat.mainmenu.views.IMainMenuView;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.NewBuyoutModel;
import com.whitefly.plutocrat.models.TargetModel;
import com.whitefly.plutocrat.models.UserModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;

/**
 * Created by satjapotiamopas on 6/8/16 AD.
 */
@RunWith(AndroidJUnit4.class)
public class InitialPageInstrumentTest {

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

        UserModel model = new UserModel();
        model.id = 14;
        sessionManager.updateActiveUser(model);
    }

    @Test
    public void testNewBuyoutConnectAPI() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        Gson gson = new Gson();
        String json = "{\"id\":15,\"display_name\":\"Pavel\",\"profile_image_url\":\"/profile_images/original/missing.png\",\"registered_at\":\"2016-05-11T12:04:31Z\",\"attacking_current_user\":false,\"successful_buyouts_count\":0,\"matched_buyouts_count\":0,\"available_shares_count\":0,\"under_buyout_threat\":false,\"defeated_at\":null,\"is_plutocrat\":false,\"active_inbound_buyout\":null,\"terminal_buyout\":null}";
        TargetModel model = gson.fromJson(json, TargetModel.class);

        MainMenuPresenter presenter = new MainMenuPresenter(mContext, new IMainMenuView() {
            @Override
            public void goToLogin() {

            }

            @Override
            public void goToShareFromInitiate() {

            }

            @Override
            public void callInitiateDialog(TargetModel target, NewBuyoutModel newBuyout) {
                assertNotNull(newBuyout);

                signal.countDown();
            }

            @Override
            public void toast(String text) {

            }

            @Override
            public void buyIAP(String sku, String payload) {

            }

            @Override
            public void handleLoadingDialog(boolean isShow) {

            }

            @Override
            public void closeInitiatePage(boolean isSuccess) {

            }

            @Override
            public void handleError(String title, String message, MetaModel meta) {

            }
        }, null, null);
        presenter.onEngageClick(new EngageClickEvent(model));

        signal.await();
    }

    @Test
    public void testExecuteBuyoutConnectAPI() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        final boolean expectedValue = false;
        int initiate_user_id = 1;
        int amount = 10;

        MainMenuPresenter presenter = new MainMenuPresenter(mContext, new IMainMenuView() {
            @Override
            public void goToLogin() {

            }

            @Override
            public void goToShareFromInitiate() {

            }

            @Override
            public void callInitiateDialog(TargetModel target, NewBuyoutModel newBuyout) {

            }

            @Override
            public void toast(String text) {

            }

            @Override
            public void buyIAP(String sku, String payload) {

            }

            @Override
            public void handleLoadingDialog(boolean isShow) {

            }

            @Override
            public void closeInitiatePage(boolean isSuccess) {
                assertEquals(expectedValue, isSuccess);

                signal.countDown();
            }

            @Override
            public void handleError(String title, String message, MetaModel meta) {

            }
        }, null, null);
        presenter.onExecuteBuyout(new ExecuteShareEvent(initiate_user_id, amount));

        signal.await();
    }
}
