package com.whitefly.plutocrat.models;

/**
 * Created by satjapotiamopas on 6/1/16 AD.
 */
public class IAPPurchaseModel {
    public transient String signedData;
    public String productId;
    public boolean autoRenewing;
    public String orderId;
    public long purchaseTime;
    public int purchaseState;
    public String developerPayload;
    public String purchaseToken;
    public String productSignature;
}
