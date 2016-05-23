package com.whitefly.plutocrat.helpers;

import com.squareup.otto.Bus;

/**
 * Created by Satjapot on 5/5/16 AD.
 *
 * Use for otto Bus singleton
 */
public class EventBus {
    private static Bus mBus;

    public static Bus getInstance() {
        if(mBus == null) {
            mBus = new Bus();
        }
        return mBus;
    }
}
