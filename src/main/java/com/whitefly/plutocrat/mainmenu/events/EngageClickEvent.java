package com.whitefly.plutocrat.mainmenu.events;

import com.whitefly.plutocrat.models.BuyoutModel;
import com.whitefly.plutocrat.models.TargetModel;

/**
 * Created by Satjapot on 5/19/16 AD.
 */
public class EngageClickEvent {
    private TargetModel mTarget;

    public TargetModel getTargetModel() {
        return mTarget;
    }

    public EngageClickEvent(TargetModel target) {
        mTarget = target;
    }

    public EngageClickEvent(BuyoutModel buyout) {
        // Debug
        TargetModel model = new TargetModel();
        model.picProfile = buyout.picProfile;
        model.name = buyout.name;
        model.daySurvived = buyout.hours;
        model.numBuyouts = buyout.shares;
        model.numThreats = 2;
        model.isPlutocrat = false;

        mTarget = model;
    }
}
