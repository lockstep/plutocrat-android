package com.whitefly.plutocrat.login.events;

import com.whitefly.plutocrat.login.views.ILoginView;

/**
 * Created by Satjapot on 5/6/16 AD.
 */
public class BackToLoginEvent {

    // Attributes
    private ILoginView.ViewState mState;

    // Getter Methods
    public ILoginView.ViewState getState() {
        return mState;
    }

    // Constructor
    public BackToLoginEvent(ILoginView.ViewState state) {
        mState = state;
    }
}
