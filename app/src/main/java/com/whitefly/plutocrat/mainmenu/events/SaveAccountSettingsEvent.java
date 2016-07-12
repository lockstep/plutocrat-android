package com.whitefly.plutocrat.mainmenu.events;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;
import com.whitefly.plutocrat.mainmenu.views.IAccountSettingView;

/**
 * Created by satjapotiamopas on 5/25/16 AD.
 */
public class SaveAccountSettingsEvent {
    private transient Bitmap mProfilePicture;

    @SerializedName("display_name")
    private String mDisplayName;

    @SerializedName("email")
    private String mEmail;

    @SerializedName("password")
    private String mNewPassword;

    @SerializedName("current_password")
    private String mCurrentPassword;

    @SerializedName("transactional_emails_enabled")
    private boolean mIsTransactionEmailEnabled;

    @SerializedName("product_emails_enabled")
    private boolean mIsProductEmailEnabled;

    private transient IAccountSettingView mResponseView;

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

    public String getCurrentPassword() {
        return mCurrentPassword;
    }

    public boolean isTransactionEmailEnabled() {
        return mIsTransactionEmailEnabled;
    }

    public boolean isProductEmailEnabled() {
        return mIsProductEmailEnabled;
    }

    public IAccountSettingView getResponseView() {
        return mResponseView;
    }

    public SaveAccountSettingsEvent(Bitmap profilePicture, String displayName, String email,
                                    String newPassword, String currentPassword,
                                    boolean isTransactionEmailEnabled, boolean isProductEmailEnabled,
                                    IAccountSettingView responseView) {
        mProfilePicture = profilePicture;
        mDisplayName = displayName;
        mEmail = email;
        mNewPassword = newPassword;
        mCurrentPassword = currentPassword;
        mIsTransactionEmailEnabled = isTransactionEmailEnabled;
        mIsProductEmailEnabled = isProductEmailEnabled;
        mResponseView = responseView;
    }
}
