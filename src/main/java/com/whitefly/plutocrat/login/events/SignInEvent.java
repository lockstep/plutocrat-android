package com.whitefly.plutocrat.login.events;

import com.whitefly.plutocrat.login.models.LoginRequestModel;

/**
 * Created by Satjapot on 5/5/16 AD.
 */
public class SignInEvent {

    // Attributes
    private String mEmail;
    private String mPassword;

    // Getter Methods
    public String getEmail() {
        return mEmail;
    }

    public String getPassword() {
        return mPassword;
    }

    // Constructor
    public SignInEvent(String email, String password) {
        mEmail = email;
        mPassword = password;
    }

    // Public
    public LoginRequestModel getRequestModel() {
        LoginRequestModel model = new LoginRequestModel();
        model.email = mEmail;
        model.password = mPassword;

        return model;
    }
}
