package com.whitefly.plutocrat.mainmenu.views;

import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.NewBuyoutModel;
import com.whitefly.plutocrat.models.TargetModel;

/**
 * Created by Satjapot on 5/12/16 AD.
 */
public interface IMainMenuView {
    void goToLogin();
    void goToShareFromInitiate();
    void callInitiateDialog(TargetModel target, NewBuyoutModel newBuyout);
    void toast(String text);
    void buyIAP(String sku, String payload);
    void handleLoadingDialog(boolean isShow);
    void handleError(String title, String message, MetaModel meta);
    void closeInitiatePage(boolean isSuccess);
}
