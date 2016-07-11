package com.whitefly.plutocrat.mainmenu.events;

/**
 * Created by Satjapot on 5/18/16 AD.
 */
public class LoadBuyoutsEvent {
    // Attributes
    public int page;
    public int perpage;

    // Constructor
    public LoadBuyoutsEvent(int page, int perpage) {
        this.page = page;
        this.perpage = perpage;
    }
}
