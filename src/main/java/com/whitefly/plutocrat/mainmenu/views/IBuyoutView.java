package com.whitefly.plutocrat.mainmenu.views;

import com.whitefly.plutocrat.models.BuyoutModel;
import com.whitefly.plutocrat.models.MetaModel;

import java.util.ArrayList;

/**
 * Created by Satjapot on 5/18/16 AD.
 */
public interface IBuyoutView {
    void setBuyoutList(ArrayList<BuyoutModel> list, MetaModel meta);
}
