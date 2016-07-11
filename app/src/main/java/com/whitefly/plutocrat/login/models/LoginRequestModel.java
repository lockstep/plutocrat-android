package com.whitefly.plutocrat.login.models;

/**
 * Created by Satjapot on 5/9/16 AD.
 */
public class LoginRequestModel {
    // Attributes
    public String email;
    public String password;

    // Constructor
    public LoginRequestModel() { }

    public LoginRequestModel(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
