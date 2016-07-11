package com.whitefly.plutocrat.models;

import android.support.annotation.Nullable;

import java.text.NumberFormat;

/**
 * Created by satjapotiamopas on 5/30/16 AD.
 */
public class ShareBundleModel {

    private static final String STRING_EMPTY = "";
    private static final String REG_DECIMAL_NUMBER = "([+-]?(\\d+\\.)?\\d+)";
    private static final String REG_POINT_AND_COMMA = "([,|.])";
    private static final String CURRENCY_FORMAT = "%s%s";
    private static final double MILLION = Math.pow(10., 6.);
    private static final int FRACTION_DIGITS = 2;
    private static final float FRACTION_CEILING_FIX = 10f;

    public enum State {
        Checking, Get, Buy
    }

    // Attributes
    public String sku;
    public int qty;
    public float price;
    public float priceTotal;
    public String currencySymbol;
    public State state;

    @Nullable
    public IAPPurchaseModel purchaseData;

    // Constructor
    public ShareBundleModel(String sku, int qty, float total) {
        this.sku = sku;
        this.qty = qty;
        this.price = (float) (Math.ceil((total * FRACTION_CEILING_FIX) / (float) qty) / FRACTION_CEILING_FIX);
        this.priceTotal = total;
        this.currencySymbol = "$";
        this.state = State.Checking;
        this.purchaseData = null;
    }

    public ShareBundleModel(IAPItemDetailModel iapItem) {
        this.sku = iapItem.productId;
        this.qty = Integer.parseInt(iapItem.description);
        this.priceTotal = (float) (Long.parseLong(iapItem.priceAmountMicro) / MILLION);
        this.price = (float) (Math.ceil((priceTotal * FRACTION_CEILING_FIX) / (float) qty) / FRACTION_CEILING_FIX);
        this.currencySymbol = iapItem.price.replaceAll(REG_DECIMAL_NUMBER, STRING_EMPTY).replaceAll(REG_POINT_AND_COMMA, STRING_EMPTY);
        this.state = State.Checking;
        this.purchaseData = null;
    }

    // Methods
    public float getTotal() {
        return priceTotal;
    }

    public String getPrice() {
        String result = STRING_EMPTY;
        float value = price;
        NumberFormat currencyFormat = NumberFormat.getNumberInstance();

        currencyFormat.setMinimumFractionDigits(FRACTION_DIGITS);
        currencyFormat.setMaximumFractionDigits(FRACTION_DIGITS);
        result = String.format(CURRENCY_FORMAT, currencySymbol, currencyFormat.format(value));
        return result;
    }

    public String getTotalPrice() {
        String result = STRING_EMPTY;
        float value = priceTotal;
        NumberFormat currencyFormat = NumberFormat.getNumberInstance();

        currencyFormat.setMinimumFractionDigits(FRACTION_DIGITS);
        currencyFormat.setMaximumFractionDigits(FRACTION_DIGITS);
        result = String.format(CURRENCY_FORMAT, currencySymbol, currencyFormat.format(value));
        return result;
    }
}
