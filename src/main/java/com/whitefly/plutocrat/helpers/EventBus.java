package com.whitefly.plutocrat.helpers;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.util.ArrayList;

/**
 * Created by Satjapot on 5/5/16 AD.
 *
 * Use for otto Bus singleton
 */
public class EventBus {
    private static Bus mBus;
    private static ArrayList<Object> mObjectRegisterdList = new ArrayList<>();

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

    public static void register(Object object) {
        if(object == null) return;

        Bus bus = getInstance();
        if(! mObjectRegisterdList.contains(object)) {
            mObjectRegisterdList.add(object);
            bus.register(object);
        }
    }

    public static void unregister(Object object) {
        if(object == null) return;

        Bus bus = getInstance();
        bus.unregister(object);

        if(mObjectRegisterdList.contains(object)) {
            mObjectRegisterdList.remove(object);
        }
    }
}
