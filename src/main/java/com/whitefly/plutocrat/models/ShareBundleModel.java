package com.whitefly.plutocrat.models;

/**
 * Created by satjapotiamopas on 5/30/16 AD.
 */
public class ShareBundleModel {

    // Attributes
    public String sku;
    public int qty;
    public int price;

    // Constructor
    public ShareBundleModel() {}

    public ShareBundleModel(String sku, int qty, int price) {
        this.sku = sku;
        this.qty = qty;
        this.price = price;
    }

    // Methods
    public int getTotal() {
        return qty * price;
    }
}
