package com.whitefly.plutocrat.mainmenu.events;

import android.graphics.Bitmap;

import com.whitefly.plutocrat.mainmenu.views.IAccountSettingView;

/**
 * Created by satjapotiamopas on 7/12/16 AD.
 */
public class SaveImageProfileEvent {
    private Bitmap mProfilePicture;
    private IAccountSettingView mResponseView;

    public IAccountSettingView getResponseView() {
        return mResponseView;
    }

    public Bitmap getProfilePicture() {
        return mProfilePicture;
    }

    public SaveImageProfileEvent(Bitmap profile, IAccountSettingView responseView) {
        mProfilePicture = profile;
        mResponseView = responseView;
    }
}
