package com.whitefly.plutocrat.login.events;

import com.whitefly.plutocrat.login.fragments.Login2Fragment;

/**
 * Created by satjapotiamopas on 7/8/16 AD.
 */
public class ChangeLoginStateEvent {
    private Login2Fragment.ViewState mState;

    public Login2Fragment.ViewState getState() {
        return mState;
    }

    public ChangeLoginStateEvent(Login2Fragment.ViewState state) {
        mState = state;
    }
}
