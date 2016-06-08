package com.whitefly.plutocrat.mainmenu.views.events;

import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.TargetModel;

import java.util.ArrayList;

/**
 * Created by satjapotiamopas on 6/20/16 AD.
 */
public class LoadTargetCompletedEvent {
    private ArrayList<TargetModel> mItems;
    private MetaModel mMeta;

    public ArrayList<TargetModel> getItems() {
        return mItems;
    }

    public MetaModel getMeta() {
        return mMeta;
    }

    public LoadTargetCompletedEvent(ArrayList<TargetModel> items, MetaModel meta) {
        mItems = items;
        mMeta = meta;
    }
}
