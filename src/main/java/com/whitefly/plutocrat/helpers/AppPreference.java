package com.whitefly.plutocrat.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by Satjapot on 5/9/16 AD.
 * This class will keep all persistence data to use in this app.
 */
public class AppPreference {
    public static final String DEBUG_APP = "Plutocrat";

    private static final String PREF_NAME = "com.whitefly.plutocat.prefs";

    // Attributes
    private static AppPreference singleton;
    private Gson mGson;

    private SharedPreferences mPrefs;
    private SessionManager mSession;

    // Get Methods
    public SharedPreferences getSharedPreference() {
        return mPrefs;
    }

    public SessionManager getSession() {
        return mSession;
    }

    // Constructor
    public AppPreference(SharedPreferences prefs) {
        mPrefs = prefs;
        mGson = new Gson();
        mSession = new SessionManager();
    }

    // Static Methods
    public static AppPreference createInstance(Context context) {
        singleton = new AppPreference(context.getSharedPreferences(PREF_NAME, 0));
        return singleton;
    }

    public static AppPreference getInstance() {
        return singleton;
    }

    // Methods

    public void savePrefs(String key, Object object, Type type) {
        mPrefs.edit()
                .putString(key, mGson.toJson(object, type))
                .commit();
    }

    public <T extends Object> T loadPrefs(String key, Class<T> classType) {
        T result = mGson.fromJson(mPrefs.getString(key, "{}"), classType);
        return result;
    }
}
