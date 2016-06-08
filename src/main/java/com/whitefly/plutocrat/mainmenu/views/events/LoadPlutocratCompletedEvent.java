package com.whitefly.plutocrat.mainmenu.views.events;

import com.whitefly.plutocrat.models.TargetModel;

/**
 * Created by satjapotiamopas on 6/20/16 AD.
 */
public class LoadPlutocratCompletedEvent {
    private TargetModel mPlutocrat;

    public TargetModel getPlutocratUser() {
        return mPlutocrat;
    }

    public LoadPlutocratCompletedEvent(TargetModel plutocrat) {
        mPlutocrat = plutocrat;
    }
}
