package com.whitefly.plutocrat.mainmenu.views.events;

import com.whitefly.plutocrat.models.BuyoutModel;
import com.whitefly.plutocrat.models.MetaModel;

import java.util.ArrayList;

/**
 * Created by satjapotiamopas on 6/20/16 AD.
 */
public class LoadBuyoutCompletedEvent {
    private ArrayList<BuyoutModel> mItems;
    private MetaModel mMeta;

    public ArrayList<BuyoutModel> getItems() {
        return mItems;
    }

    public MetaModel getMeta() {
        return mMeta;
    }

    public LoadBuyoutCompletedEvent(ArrayList<BuyoutModel> items, MetaModel meta) {
        mItems = items;
        mMeta = meta;
    }
}
