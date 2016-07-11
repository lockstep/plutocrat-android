package com.whitefly.plutocrat.mainmenu.events;

/**
 * Created by satjapotiamopas on 6/8/16 AD.
 */
public class ExecuteShareEvent {
    private int mInitiateUserId;
    private int mNumShares;

    public int getInitiateUserId() {
        return mInitiateUserId;
    }

    public int getNumberOfShare() {
        return mNumShares;
    }

    public ExecuteShareEvent(int userId, int count) {
        mInitiateUserId = userId;
        mNumShares = count;
    }
}
