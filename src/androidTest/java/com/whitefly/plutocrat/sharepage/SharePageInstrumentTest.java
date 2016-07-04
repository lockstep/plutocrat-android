package com.whitefly.plutocrat.sharepage;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.SessionManager;
import com.whitefly.plutocrat.mainmenu.events.SendReceiptCompleteEvent;
import com.whitefly.plutocrat.mainmenu.events.SendReceiptEvent;
import com.whitefly.plutocrat.mainmenu.presenters.MainMenuPresenter;
import com.whitefly.plutocrat.mainmenu.views.IMainMenuView;
import com.whitefly.plutocrat.models.IAPPurchaseModel;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.NewBuyoutModel;
import com.whitefly.plutocrat.models.TargetModel;
import com.whitefly.plutocrat.models.UserModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import static org.junit.Assert.*;

/**
 * Created by satjapotiamopas on 6/22/16 AD.
 */
@RunWith(AndroidJUnit4.class)
public class SharePageInstrumentTest {
    public static final String DEVELOPER_PAYLOAD = "thisisdeveloperpayloadfortestsendreceipt";
    public static final String IAP_SIGNATURE = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlPnYdpcAodnLSLUZiWcPJZQYeCFVew4wk08SIv21wzxdKL1Mmo79TvSVmccU1lUSMlvVQ5JEwuAe39ebu7gZlLZPHg7dkTlvaffDeRvlMZEugnVboXR/KEj1a+f5FNDrdC+D6RAMF2o/6p+bhBVt06FeWiSGx868g4CyBg8SfWnuUUDq2gIEuOLlQhoPzCTEPsN37nqcnX8mKKX2Juv3cJSNeUoRieg2JKjudDih8d6G0hvURYjHQv0AidzrPiSpYh6+oc+jgJxtSn4iRAHJu76P6NeSnMBUqeMKkK1JU2PSQGLHZVhWGEEh5gm+V6xO5t0y87KIxTjPF6rqEPxpkwIDAQAB";

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

        UserModel model = new UserModel();
        model.id = 14;
        sessionManager.updateActiveUser(model);
    }

    @Test
    public void testSendDuplicatedReceipt() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        final MainMenuPresenter presenter = new MainMenuPresenter(mContext, new IMainMenuView() {
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
            public void buyIAP(String sku, @Nullable String payload) {

            }

            @Override
            public void handleLoadingDialog(boolean isShow) {

            }

            @Override
            public void handleError(String title, String message, MetaModel meta) {

            }

            @Override
            public void closeInitiatePage(boolean isSuccess) {

            }
        }, null, null);
        Object callback = new Object() {
            @Subscribe
            public void onSendReceiptComplete(SendReceiptCompleteEvent event) {
                assertEquals(SendReceiptCompleteEvent.State.Duplicated, event.getState());

                EventBus.getInstance().unregister(presenter);
                EventBus.getInstance().unregister(this);
                signal.countDown();
            }
        };
        IAPPurchaseModel mockData = new IAPPurchaseModel();
        mockData.productId = "mock.test1";
        mockData.orderId = "GPA.order_id_1";
        mockData.purchaseToken = String.format("receipt_token:%s:%s", mockData.productId, mockData.orderId);
        mockData.autoRenewing = false;
        mockData.developerPayload = DEVELOPER_PAYLOAD;
        mockData.productSignature = IAP_SIGNATURE;

        EventBus.getInstance().register(presenter);
        EventBus.getInstance().register(callback);
        EventBus.getInstance().post(new SendReceiptEvent(mockData));
        signal.await();
    }

    @Test
    public void testSendReceiptFailOrderIdInvalid() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        final MainMenuPresenter presenter = new MainMenuPresenter(mContext, new IMainMenuView() {
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
            public void buyIAP(String sku, @Nullable String payload) {

            }

            @Override
            public void handleLoadingDialog(boolean isShow) {

            }

            @Override
            public void handleError(String title, String message, MetaModel meta) {

            }

            @Override
            public void closeInitiatePage(boolean isSuccess) {

            }
        }, null, null);
        Object callback = new Object() {
            @Subscribe
            public void onSendReceiptComplete(SendReceiptCompleteEvent event) {
                if(event.getState() == SendReceiptCompleteEvent.State.Succeed) {
                    fail("Should not pass");
                } else {
                    Log.d(AppPreference.DEBUG_APP, "Error IAP: " + event.getMetaError().getErrors());
                    boolean isFound = false;
                    Set<String> keys = event.getMetaError().getKeys();
                    for (String key : keys) {
                        if(key.equals("")) {
                            isFound = true;
                            break;
                        }
                    }
                    assertTrue(isFound);
                }

                EventBus.getInstance().unregister(presenter);
                EventBus.getInstance().unregister(this);
                signal.countDown();
            }
        };
        IAPPurchaseModel mockData = new IAPPurchaseModel();
        mockData.productId = "mock.test1";
        mockData.orderId = "GPA.order_id_1";
        mockData.purchaseToken = String.format("receipt_token:%s:%s", mockData.productId, mockData.orderId);
        mockData.autoRenewing = false;
        mockData.developerPayload = DEVELOPER_PAYLOAD;
        mockData.productSignature = IAP_SIGNATURE;

        EventBus.getInstance().register(presenter);
        EventBus.getInstance().register(callback);
        EventBus.getInstance().post(new SendReceiptEvent(mockData));
        signal.await();
    }

}
