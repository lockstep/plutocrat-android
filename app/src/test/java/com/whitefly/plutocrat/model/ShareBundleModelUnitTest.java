package com.whitefly.plutocrat.model;

import com.whitefly.plutocrat.models.IAPItemDetailModel;
import com.whitefly.plutocrat.models.ShareBundleModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;
/**
 * Created by satjapotiamopas on 7/1/16 AD.
 */
@RunWith(JUnit4.class)
public class ShareBundleModelUnitTest {
    private static final double MILLION = Math.pow(10., 6.);

    private String[] mExpectedProductIds;
    private String[] mExpectedPrices;
    private String[] mExpectedTotals;
    private String mCurrencySymbol;
    private int[] mQuantities;

    @Before
    public void setupExpectedValue() {
        mExpectedProductIds = new String[] {
                "com.whiteflyventuresinc.plutocrat.shares.1",
                "com.whiteflyventuresinc.plutocrat.shares.10",
                "com.whiteflyventuresinc.plutocrat.shares.50",
                "com.whiteflyventuresinc.plutocrat.shares.100",
                "com.whiteflyventuresinc.plutocrat.shares.500",
        };
        mExpectedPrices = new String[] {
                "1.00", "1.00", "0.90", "0.80", "0.60"
        };
        mExpectedTotals = new String[] {
                "0.99", "9.99", "44.99", "79.99", "299.00"
        };
        mQuantities = new int[] { 1, 10, 50, 100, 500 };
    }

    @Test
    public void testGetPriceStringWithRawConstructor() {
        mCurrencySymbol = "$";

        for(int i=0, n=mExpectedProductIds.length; i<n; i++) {
            String expectedProductId = mExpectedProductIds[i];
            String expectedPrice = mCurrencySymbol + mExpectedPrices[i];
            String expectedTotal = mCurrencySymbol + mExpectedTotals[i];

            int qty = mQuantities[i];
            float price = Float.valueOf(mExpectedTotals[i]);

            ShareBundleModel model = new ShareBundleModel(expectedProductId, qty, price);

            assertEquals(expectedProductId, model.sku);
            assertEquals(expectedPrice, model.getPrice());
            assertEquals(expectedTotal, model.getTotalPrice());
        }
    }

    @Test
    public void testGetPriceStringWithIAPItemDetailModelConstructor() {
        mCurrencySymbol = "$";
        String currency = "USD";

        for(int i=0, n=mExpectedProductIds.length; i<n; i++) {
            String expectedProductId = mExpectedProductIds[i];
            String expectedPrice = mCurrencySymbol + mExpectedPrices[i];
            String expectedTotal = mCurrencySymbol + mExpectedTotals[i];
            int quantity = mQuantities[i];

            IAPItemDetailModel inputModel = new IAPItemDetailModel();
            inputModel.productId = expectedProductId;
            inputModel.title = "some title";
            inputModel.description = String.valueOf(quantity);
            inputModel.type = "inapp";
            inputModel.price = expectedPrice;
            inputModel.priceAmountMicro = String.valueOf((long) (Double.parseDouble(mExpectedTotals[i]) * MILLION));
            inputModel.priceCurrencyCode = currency;

            ShareBundleModel model = new ShareBundleModel(inputModel);

            assertEquals(expectedProductId, model.sku);
            assertEquals(expectedPrice, model.getPrice());
            assertEquals(expectedTotal, model.getTotalPrice());
        }
    }

    @Test
    public void testGetPriceStringWithLocalizedIAPItemDetailModelConstructor() {
        mCurrencySymbol = "THB";
        String currency = "THB";
        mExpectedPrices = new String[] {
                "35.00", "32.50", "31.50", "28.00", "21.00"
        };
        mExpectedTotals = new String[] {
                "35.00", "325.00", "1,575.00", "2,800.00", "10,500.00"
        };
        String[] totalsWithoutComma = new String [] {
                "35.00", "325.00", "1575.00", "2800.00", "10500.00"
        };

        for(int i=0, n=mExpectedProductIds.length; i<n; i++) {
            String expectedProductId = mExpectedProductIds[i];
            String expectedPrice = mCurrencySymbol + mExpectedPrices[i];
            String expectedTotal = mCurrencySymbol + mExpectedTotals[i];
            int quantity = mQuantities[i];

            IAPItemDetailModel inputModel = new IAPItemDetailModel();
            inputModel.productId = expectedProductId;
            inputModel.title = "some title";
            inputModel.description = String.valueOf(quantity);
            inputModel.type = "inapp";
            inputModel.price = expectedPrice;
            inputModel.priceAmountMicro = String.valueOf((long) (Double.parseDouble(totalsWithoutComma[i]) * MILLION));
            inputModel.priceCurrencyCode = currency;

            ShareBundleModel model = new ShareBundleModel(inputModel);

            assertEquals(expectedProductId, model.sku);
            assertEquals(expectedPrice, model.getPrice());
            assertEquals(expectedTotal, model.getTotalPrice());
        }
    }
}
