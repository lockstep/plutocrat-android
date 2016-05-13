package com.whitefly.plutocrat.login.views;

/**
 * Created by Satjapot on 5/5/16 AD.
 */
public interface ILoginView {
    enum ViewState {
        Login, Register
    }

    void changeState(ViewState state);
    void toast(String text);
}
