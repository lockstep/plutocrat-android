package com.whitefly.plutocrat.models;

/**
 * Created by satjapotiamopas on 5/30/16 AD.
 */
public class ShareBundleModel {

    // Attributes
    public String sku;
    public int qty;
    public float price;

    // Constructor
    public ShareBundleModel() {}

    public ShareBundleModel(String sku, int qty, float price) {
        this.sku = sku;
        this.qty = qty;
        this.price = price;
    }

    // Methods
    public float getTotal() {
        return qty * price;
    }
}
