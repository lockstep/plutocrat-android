package com.whitefly.plutocrat.mainmenu.events;

/**
 * Created by Satjapot on 6/4/16 AD.
 */
public class UpdateUserNoticeIdEvent {
    private int mNextNoticeId;

    public int getNextNoticeId() {
        return mNextNoticeId;
    }

    public UpdateUserNoticeIdEvent(int nextNoticeId) {
        mNextNoticeId = nextNoticeId;
    }
}
