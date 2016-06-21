package com.whitefly.plutocrat;

import android.app.Application;

import com.whitefly.plutocrat.helpers.AppPreference;

/**
 * Created by Satjapot on 6/19/16 AD.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppPreference.createInstance(this);
    }
}
