package com.whitefly.plutocrat.mainmenu.events;

import com.whitefly.plutocrat.models.ShareBundleModel;

/**
 * Created by satjapotiamopas on 5/24/16 AD.
 */
public class BuySharesEvent {
    private ShareBundleModel mModel;

    public ShareBundleModel getBundleDetail() {
        return mModel;
    }

    public BuySharesEvent(ShareBundleModel model) {
        mModel = model;
    }
}
