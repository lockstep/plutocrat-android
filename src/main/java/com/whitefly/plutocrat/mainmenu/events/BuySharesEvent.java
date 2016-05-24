package com.whitefly.plutocrat.mainmenu.events;

/**
 * Created by satjapotiamopas on 5/24/16 AD.
 */
public class BuySharesEvent {
    private int mPrice;
    private int mQty;

    public int getPrice() {
        return mPrice;
    }

    public int getQty() {
        return mQty;
    }

    public BuySharesEvent(int qty, int price) {
        mPrice = price;
        mQty = qty;
    }
}
