package com.whitefly.plutocrat.mainmenu.views;

import com.whitefly.plutocrat.mainmenu.fragments.HomeFragment;

/**
 * Created by Satjapot on 6/2/16 AD.
 */
public interface IHomeView {
    void changeState(HomeFragment.State state, int noticeId);
}
