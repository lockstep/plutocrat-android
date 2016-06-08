package com.whitefly.plutocrat.helper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.IAPHelper;
import com.whitefly.plutocrat.models.IAPPurchaseModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import static org.junit.Assert.*;
/**
 * Created by satjapotiamopas on 6/15/16 AD.
 */
@RunWith(AndroidJUnit4.class)
public class IAPInstrumentTest {

    private Context mContext;
    private IAPHelper mIAPHelper;

    @Before
    public void setup() {
        mContext = InstrumentationRegistry.getTargetContext();
        AppPreference.createInstance(mContext);
        mIAPHelper = new IAPHelper(mContext);
    }

    @Test
    public void testConsumeItem() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        String purchaseToken = "idklfpldknejdcilenhaokie.AO-J1OxqPh6TZz6hMxbsVwlP87p_xTRnFYGE0T8vxBU1xIi4BSMPZRXlIZXISYwj_al3GRIZcCNPvmhlh1qDcNWHMySuxPJqJfaBls8fyTbBOacyKKhHSYWt59G8jACyVB5op0vo2cphXTipIv7_oZWFZJJa82LMug";
        mIAPHelper.setIAPProcessListener(new IAPHelper.IAPProcessListener() {
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
        });

        mIAPHelper.consume(purchaseToken);
        signal.await();
    }
}
