package com.whitefly.plutocrat.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by satjapotiamopas on 6/8/16 AD.
 */
public class NewBuyoutModel {
    @SerializedName("available_shares_count")
    public int availableShareCount;

    @SerializedName("minimum_buyout_shares")
    public int minimumAmount;
}
