package com.whitefly.plutocrat.mainmenu.events;

import android.graphics.Bitmap;

/**
 * Created by satjapotiamopas on 5/25/16 AD.
 */
public class SaveAccountSettingsEvent {
    private Bitmap mProfilePicture;
    private String mDisplayName;
    private String mEmail;
    private String mNewPassword;
    private String mConfirmPassword;
    private String mCurrentPassword;
    private boolean mIsEnableNotification;
    private boolean mIsEnableUpdates;

    public Bitmap getProfilePicture() {
        return mProfilePicture;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getNewPassword() {
        return mNewPassword;
    }

    public String getConfirmPassword() {
        return mConfirmPassword;
    }

    public String getCurrentPassword() {
        return mCurrentPassword;
    }

    public boolean isEnableNotification() {
        return mIsEnableNotification;
    }

    public boolean isEnableUpdates() {
        return mIsEnableUpdates;
    }

    public SaveAccountSettingsEvent(Bitmap profilePicture, String displayName, String email,
                                    String newPassword, String confirmPassword, String currentPassword,
                                    boolean isEnableNotification, boolean isEnableUpdates) {
        mProfilePicture = profilePicture;
        mDisplayName = displayName;
        mEmail = email;
        mNewPassword = newPassword;
        mConfirmPassword = confirmPassword;
        mCurrentPassword = currentPassword;
        mIsEnableNotification = isEnableNotification;
        mIsEnableUpdates = isEnableUpdates;
    }
}
