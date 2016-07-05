package com.whitefly.plutocrat.login.events;

/**
 * Created by Satjapot on 6/23/16 AD.
 */
public class ResetPasswordEvent {
    private String mEmail;
    private String mToken;
    private String mNewPassword;
    private String mConfirmPassword;

    public String getEmail() {
        return mEmail;
    }

    public String getToken() {
        return mToken;
    }

    public String getNewPassword() {
        return mNewPassword;
    }

    public String getConfirmPassword() {
        return mConfirmPassword;
    }

    public ResetPasswordEvent(String email, String token, String newPassword, String confirmPassword) {
        mEmail = email;
        mToken = token;
        mNewPassword = newPassword;
        mConfirmPassword = confirmPassword;
    }
}
