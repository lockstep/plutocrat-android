package com.whitefly.plutocrat.mainmenu.events;

/**
 * Created by Satjapot on 5/18/16 AD.
 */
public class LoadTargetsEvent {
    // Attributes
    public int page;
    public int perpage;

    // Constructor
    public LoadTargetsEvent(int page, int perpage) {
        this.page = page;
        this.perpage = perpage;
    }
}
