package com.whitefly.plutocrat.helpers;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by Satjapot on 5/5/16 AD.
 *
 * Use for otto Bus singleton
 */
public class EventBus {
    private static Bus mBus;

    public static Bus getInstance() {
        return getInstance(null);
    }

    public static Bus getInstance(ThreadEnforcer threadEnforcer) {
        if(mBus == null) {
            if(threadEnforcer == null) {
                mBus = new Bus();
            } else {
                mBus = new Bus(threadEnforcer);
            }
        }
        return mBus;
    }
}
