package com.whitefly.plutocrat.login.views;

import com.whitefly.plutocrat.models.MetaModel;

/**
 * Created by Satjapot on 5/5/16 AD.
 */
public interface ILoginView {
    enum ViewState {
        Login, Register
    }

    void changeState(ViewState state);
    void handleError(MetaModel meta);
    void toast(String text);
}
