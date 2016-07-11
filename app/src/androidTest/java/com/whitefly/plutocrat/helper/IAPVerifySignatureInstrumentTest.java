package com.whitefly.plutocrat.helper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.IAPHelper;
import com.whitefly.plutocrat.helpers.utils.Security;
import com.whitefly.plutocrat.models.IAPItemDetailModel;
import com.whitefly.plutocrat.models.IAPPurchaseModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by satjapotiamopas on 6/24/16 AD.
 */
@RunWith(AndroidJUnit4.class)
public class IAPVerifySignatureInstrumentTest {
    private static final String PUBLIC_IAP_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlPnYdpcAodnLSLUZiWcPJZQYeCFVew4wk08SIv21wzxdKL1Mmo79TvSVmccU1lUSMlvVQ5JEwuAe39ebu7gZlLZPHg7dkTlvaffDeRvlMZEugnVboXR/KEj1a+f5FNDrdC+D6RAMF2o/6p+bhBVt06FeWiSGx868g4CyBg8SfWnuUUDq2gIEuOLlQhoPzCTEPsN37nqcnX8mKKX2Juv3cJSNeUoRieg2JKjudDih8d6G0hvURYjHQv0AidzrPiSpYh6+oc+jgJxtSn4iRAHJu76P6NeSnMBUqeMKkK1JU2PSQGLHZVhWGEEh5gm+V6xO5t0y87KIxTjPF6rqEPxpkwIDAQAB";

    private Context mContext;
    private IAPHelper mIAPHelper;

    @Before
    public void setup() {
        mContext = InstrumentationRegistry.getTargetContext();
        AppPreference.createInstance(mContext);
        mIAPHelper = new IAPHelper(mContext);
        do {
            // Wait until IAPHelper ready
        } while(! mIAPHelper.isReady());
    }

    @Test
    public void testVerifyPass() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        mIAPHelper.addIAPProcessListener(new IAPHelper.IAPProcessListener() {
            @Override
            public void onBuySuccess(int resultCode, IAPPurchaseModel model) {

            }

            @Override
            public void onBuyFailed(int resultCode) {

            }

            @Override
            public void onConsumed(int resultCode) {

            }

            @Override
            public void onProcessing(int methodId, ProcessState state) {

            }

            @Override
            public void onPurchasedItemLoaded(int resultCode, ArrayList<IAPPurchaseModel> items) {
                Log.d(AppPreference.DEBUG_APP, items.toString());
                assertEquals(IAPHelper.BILLING_RESPONSE_RESULT_OK, resultCode);

                for(IAPPurchaseModel item : items) {
                    assertTrue(Security.verifyPurchase(PUBLIC_IAP_KEY, item.signedData, item.productSignature));
                }

                signal.countDown();
            }

            @Override
            public void onItemDetailsLoaded(int resultCode, ArrayList<IAPItemDetailModel> itemDetails) {

            }
        });

        mIAPHelper.getPurchased();

        signal.await();
    }

    @Test
    public void testVerifyFailedWithWrongSignedData() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        mIAPHelper.addIAPProcessListener(new IAPHelper.IAPProcessListener() {
            @Override
            public void onBuySuccess(int resultCode, IAPPurchaseModel model) {

            }

            @Override
            public void onBuyFailed(int resultCode) {

            }

            @Override
            public void onConsumed(int resultCode) {

            }

            @Override
            public void onProcessing(int methodId, ProcessState state) {

            }

            @Override
            public void onPurchasedItemLoaded(int resultCode, ArrayList<IAPPurchaseModel> items) {
                Log.d(AppPreference.DEBUG_APP, items.toString());
                assertEquals(IAPHelper.BILLING_RESPONSE_RESULT_OK, resultCode);

                for(IAPPurchaseModel item : items) {
                    if(item.productId.equals(IAPHelper.IAP_TEST_PRODUCT)) continue;
                    assertFalse(Security.verifyPurchase(PUBLIC_IAP_KEY, item.signedData + "a", item.productSignature));
                }

                signal.countDown();
            }

            @Override
            public void onItemDetailsLoaded(int resultCode, ArrayList<IAPItemDetailModel> itemDetails) {

            }
        });

        mIAPHelper.getPurchased();
        signal.await();
    }

    @Test
    public void testVerifyFailedWithWrongSignature() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        mIAPHelper.addIAPProcessListener(new IAPHelper.IAPProcessListener() {
            @Override
            public void onBuySuccess(int resultCode, IAPPurchaseModel model) {

            }

            @Override
            public void onBuyFailed(int resultCode) {

            }

            @Override
            public void onConsumed(int resultCode) {

            }

            @Override
            public void onProcessing(int methodId, ProcessState state) {

            }

            @Override
            public void onPurchasedItemLoaded(int resultCode, ArrayList<IAPPurchaseModel> items) {
                Log.d(AppPreference.DEBUG_APP, items.toString());
                assertEquals(IAPHelper.BILLING_RESPONSE_RESULT_OK, resultCode);

                for(IAPPurchaseModel item : items) {
                    if(item.productId.equals(IAPHelper.IAP_TEST_PRODUCT)) continue;
                    assertFalse(Security.verifyPurchase(PUBLIC_IAP_KEY, item.signedData, item.productSignature + "a"));
                }

                signal.countDown();
            }

            @Override
            public void onItemDetailsLoaded(int resultCode, ArrayList<IAPItemDetailModel> itemDetails) {

            }
        });

        mIAPHelper.getPurchased();
        signal.await();
    }
}
