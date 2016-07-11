package com.whitefly.plutocrat.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by satjapotiamopas on 6/30/16 AD.
 */
public class IAPItemDetailModel {
    public String productId;
    public String title;
    public String description;
    public String type;
    public String price;

    @SerializedName("price_amount_micros")
    public String priceAmountMicro;

    @SerializedName("price_currency_code")
    public String priceCurrencyCode;
}
