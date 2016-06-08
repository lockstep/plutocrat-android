package com.whitefly.plutocrat.mainmenu.views.events;

/**
 * Created by satjapotiamopas on 6/20/16 AD.
 */
public class MatchBuyoutCompletedEvent {
    public enum Result {
        Matched, Failed
    }

    private Result mResult;

    public Result getResult() {
        return mResult;
    }

    public MatchBuyoutCompletedEvent(Result result) {
        mResult = result;
    }
}
