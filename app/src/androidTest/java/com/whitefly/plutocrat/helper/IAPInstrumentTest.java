package com.whitefly.plutocrat.helper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.IAPHelper;
import com.whitefly.plutocrat.models.IAPItemDetailModel;
import com.whitefly.plutocrat.models.IAPPurchaseModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import static org.junit.Assert.*;
/**
 * Created by satjapotiamopas on 6/15/16 AD.
 */
@RunWith(AndroidJUnit4.class)
public class IAPInstrumentTest {
    private static final String DEVELOPER_PAYLOAD = "thisisaniaptest";

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
    public void testGetProductDetails() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        final String[] expectedIds = mContext.getResources().getStringArray(R.array.iap_item_list);
        final int expectedRowCount = expectedIds.length;

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

            }

            @Override
            public void onItemDetailsLoaded(int resultCode, ArrayList<IAPItemDetailModel> itemDetails) {
                assertEquals(IAPHelper.BILLING_RESPONSE_RESULT_OK, resultCode);
                assertEquals(expectedRowCount, itemDetails.size());

                int i=0;
                for(IAPItemDetailModel model : itemDetails) {
                    boolean isFound = false;
                    for (String expectedProductId : expectedIds) {
                        if(expectedProductId.equals(model.productId)) {
                            isFound = true;
                            break;
                        }
                    }
                    assertTrue(isFound);
                    assertNotNull(model.title);
                    assertNotNull(model.description);
                    assertNotNull(model.price);
                    assertNotNull(model.priceAmountMicro);
                    assertNotNull(model.priceCurrencyCode);
                    assertNotNull(model.type);
                }

                signal.countDown();
            }
        });

        mIAPHelper.getItemDetails(expectedIds);
        signal.await();
    }

    @Test
    public void testGetPurchasedItem() throws Exception {
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
    public void testConsumeSampleItem() throws Exception {
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
                assertEquals(0, resultCode);

                signal.countDown();
            }

            @Override
            public void onProcessing(int methodId, ProcessState state) {

            }

            @Override
            public void onPurchasedItemLoaded(int resultCode, ArrayList<IAPPurchaseModel> items) {
                if(resultCode == IAPHelper.BILLING_RESPONSE_RESULT_OK) {
                    if(items.size() > 0) {
                        IAPPurchaseModel item = items.get(0);
                        mIAPHelper.consume(item.purchaseToken);
                    } else {
                        assertEquals(IAPHelper.BILLING_RESPONSE_RESULT_OK, resultCode);
                        signal.countDown();
                    }
                } else {
                    fail("Google Play error with code: " + String.valueOf(resultCode));
                    signal.countDown();
                }
            }

            @Override
            public void onItemDetailsLoaded(int resultCode, ArrayList<IAPItemDetailModel> itemDetails) {

            }
        });

        mIAPHelper.getPurchased();
        signal.await();
    }
}
