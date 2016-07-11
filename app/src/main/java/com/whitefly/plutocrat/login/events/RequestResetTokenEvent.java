package com.whitefly.plutocrat.login.events;

/**
 * Created by Satjapot on 5/6/16 AD.
 */
public class RequestResetTokenEvent {

    // Attributes
    private String mEmail;
    private boolean mSubmitClick;

    // Getter Methods
    public String getEmail() {
        return mEmail;
    }

    public boolean isSubmitClick() {
        return mSubmitClick;
    }

    // Constructor
    public RequestResetTokenEvent(String email, boolean isSubmit) {
        mEmail = email;
        mSubmitClick = isSubmit;
    }
}
