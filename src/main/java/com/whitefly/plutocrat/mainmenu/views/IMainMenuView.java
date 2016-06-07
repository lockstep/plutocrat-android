package com.whitefly.plutocrat.mainmenu.views;

import com.whitefly.plutocrat.models.TargetModel;

/**
 * Created by Satjapot on 5/12/16 AD.
 */
public interface IMainMenuView {
    void goToLogin();
    void goToShareFromInitiate();
    void callInitiateDialog(TargetModel model);
    void toast(String text);
    void buyIAP(String sku, String payload);
}
