package com.whitefly.plutocrat.mainmenu.events;

import com.whitefly.plutocrat.models.IAPPurchaseModel;

/**
 * Created by satjapotiamopas on 6/22/16 AD.
 */
public class SendReceiptEvent {
    private IAPPurchaseModel mItem;

    public IAPPurchaseModel getPurchasedItem() {
        return mItem;
    }

    public SendReceiptEvent(IAPPurchaseModel item) {
        mItem = item;
    }
}
