package com.whitefly.plutocrat.mainmenu.views.events;

import com.whitefly.plutocrat.mainmenu.fragments.HomeFragment;

/**
 * Created by satjapotiamopas on 6/20/16 AD.
 */
public class UpdateHomeViewEvent {
    private HomeFragment.State mState;
    private int mNoticeId;

    public HomeFragment.State getHomeState() {
        return mState;
    }

    public int getNoticeId() {
        return mNoticeId;
    }

    public UpdateHomeViewEvent(HomeFragment.State state, int noticeId) {
        mState = state;
        mNoticeId = noticeId;
    }
}
