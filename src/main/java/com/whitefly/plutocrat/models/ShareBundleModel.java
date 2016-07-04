package com.whitefly.plutocrat.models;

import android.support.annotation.Nullable;

import java.text.NumberFormat;

/**
 * Created by satjapotiamopas on 5/30/16 AD.
 */
public class ShareBundleModel {

    private static final double MILLION = Math.pow(10., 6.);
    private static final String REG_DECIMAL_NUMBER = "([+-]?(\\d+\\.)?\\d+)";
    private static final String CURRENCY_FORMAT = "%s%s";
    private static final int FRACTION_DIGITS = 2;

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
    public ShareBundleModel(String sku, int qty, float price) {
        this.sku = sku;
        this.qty = qty;
        this.price = price;
        this.priceTotal = qty * price;
        this.currencySymbol = "$";
        this.state = State.Checking;
        this.purchaseData = null;
    }

    public ShareBundleModel(IAPItemDetailModel iapItem) {
        this.sku = iapItem.productId;
        this.qty = Integer.parseInt(iapItem.description);
        this.priceTotal = (float) (Long.parseLong(iapItem.priceAmountMicro) / MILLION);
        this.price = this.priceTotal / this.qty;
        this.currencySymbol = iapItem.price.replaceAll(REG_DECIMAL_NUMBER, "").replaceAll("([,|.])", "");
        this.state = State.Checking;
        this.purchaseData = null;
    }

    // Methods
    public float getTotal() {
        return qty * price;
    }

    public String getPrice() {
        String result = "";
        float value = price;
        NumberFormat currencyFormat = NumberFormat.getNumberInstance();

        if(value != (int) value) {
            currencyFormat.setMinimumFractionDigits(FRACTION_DIGITS);
            currencyFormat.setMaximumFractionDigits(FRACTION_DIGITS);
        }
        result = String.format(CURRENCY_FORMAT, currencySymbol, currencyFormat.format(value));
        return result;
    }

    public String getTotalPrice() {
        String result = "";
        float value = priceTotal;
        NumberFormat currencyFormat = NumberFormat.getNumberInstance();

        if(value != (int) value) {
            currencyFormat.setMinimumFractionDigits(FRACTION_DIGITS);
            currencyFormat.setMaximumFractionDigits(FRACTION_DIGITS);
        }
        result = String.format(CURRENCY_FORMAT, currencySymbol, currencyFormat.format(value));
        return result;
    }
}
