package com.whitefly.plutocrat.model;

import com.whitefly.plutocrat.models.IAPItemDetailModel;
import com.whitefly.plutocrat.models.ShareBundleModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;
/**
 * Created by satjapotiamopas on 7/1/16 AD.
 */
@RunWith(JUnit4.class)
public class ShareBundleModelUnitTest {

    @Test
    public void testGetPriceStringWithRawConstructor() {
        String expectedProductId = "test.product.id";
        String expectedTotal = "$49.50";
        String expectedPrice = "$0.99";

        int qty = 50;
        float price = 0.99f;

        ShareBundleModel model = new ShareBundleModel(expectedProductId, qty, price);

        assertEquals(expectedProductId, model.sku);
        assertEquals(expectedPrice, model.getPrice());
        assertEquals(expectedTotal, model.getTotalPrice());
    }

    @Test
    public void testGetPriceStringWithIAPItemDetailModelConstructor() {
        String expectedProductId = "test.product.id";
        String expectedTotal = "$49.50";
        String expectedPrice = "$0.99";

        IAPItemDetailModel inputModel = new IAPItemDetailModel();
        inputModel.productId = expectedProductId;
        inputModel.title = "some title";
        inputModel.description = "50";
        inputModel.type = "inapp";
        inputModel.price = "$49.50";
        inputModel.priceAmountMicro = "49500000";
        inputModel.priceCurrencyCode = "USD";

        ShareBundleModel model = new ShareBundleModel(inputModel);

        assertEquals(expectedPrice, model.getPrice());
        assertEquals(expectedTotal, model.getTotalPrice());
    }

    @Test
    public void testGetPriceStringWithLocalizedIAPItemDetailModelConstructor() {
        String expectedProductId = "test.product.id";
        String expectedTotal = "THB5";
        String expectedPrice = "THB2.50";

        IAPItemDetailModel inputModel = new IAPItemDetailModel();
        inputModel.productId = expectedProductId;
        inputModel.title = "some title";
        inputModel.description = "2";
        inputModel.type = "inapp";
        inputModel.price = "THB5.00";
        inputModel.priceAmountMicro = "5000000";
        inputModel.priceCurrencyCode = "THB";

        ShareBundleModel model = new ShareBundleModel(inputModel);

        assertEquals(expectedPrice, model.getPrice());
        assertEquals(expectedTotal, model.getTotalPrice());
    }

    @Test
    public void testGetPriceStringWithLocalizedANDCommaIAPItemDetailModelConstructor() {
        String expectedProductId = "test.product.id";
        String expectedTotal = "THB1,000";
        String expectedPrice = "THB500";

        IAPItemDetailModel inputModel = new IAPItemDetailModel();
        inputModel.productId = expectedProductId;
        inputModel.title = "some title";
        inputModel.description = "2";
        inputModel.type = "inapp";
        inputModel.price = "THB1,000.00";
        inputModel.priceAmountMicro = "1000000000";
        inputModel.priceCurrencyCode = "THB";

        ShareBundleModel model = new ShareBundleModel(inputModel);

        assertEquals(expectedPrice, model.getPrice());
        assertEquals(expectedTotal, model.getTotalPrice());
    }
}
