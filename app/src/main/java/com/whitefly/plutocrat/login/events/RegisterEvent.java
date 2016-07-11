package com.whitefly.plutocrat.login.events;

import com.whitefly.plutocrat.login.models.RegisterRequestModel;

/**
 * Created by Satjapot on 5/5/16 AD.
 * Description: Event for register button pressed.
 */
public class RegisterEvent {

    // Attributes
    private String mDisplayName;
    private String mEmail;
    private String mPassword;

    // Getter Mehtods
    public String getDisplayName() {
        return mDisplayName;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getPassword() {
        return mPassword;
    }

    // Constructor
    public RegisterEvent(String displayName, String email, String password) {
        mDisplayName = displayName;
        mEmail = email;
        mPassword = password;
    }

    // Methods
    public RegisterRequestModel getRequestModel() {
        RegisterRequestModel model = new RegisterRequestModel();
        model.display_name = mDisplayName;
        model.email = mEmail;
        model.password = mPassword;
        model.password_confirmation = mPassword;

        return model;
    }
}
