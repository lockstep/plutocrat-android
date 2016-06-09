package com.whitefly.plutocrat.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.widget.TextView;

import com.google.gson.Gson;
import com.whitefly.plutocrat.models.TargetModel;
import com.whitefly.plutocrat.models.UserModel;

import java.lang.reflect.Type;
import java.util.EnumMap;

/**
 * Created by Satjapot on 5/9/16 AD.
 * This class will keep all persistence data to use in this app.
 */
public class AppPreference {
    public static final String DEBUG_APP = "Plutocrat";

    public static final String PREF_NAME = "com.whitefly.plutocat.prefs";

    public enum FontType {
        Regular, Bold, Italic, BoldItalic, Light, LightItalic
    }

    // Attributes
    private static AppPreference singleton;
    private Gson mGson;

    private SharedPreferences mPrefs;
    private SessionManager mSession;

    private EnumMap<FontType, Typeface> mFonts;

    private TargetModel mCurrentTarget;

    // Get/Set Methods
    public TargetModel getCurrentTarget() {
        return mCurrentTarget;
    }

    public void setCurrentTarget(TargetModel target) {
        mCurrentTarget = target;
    }

    public SharedPreferences getSharedPreference() {
        return mPrefs;
    }

    public SessionManager getSession() {
        return mSession;
    }

    public Typeface getFont(FontType type) {
        if(mFonts == null) return null;
        return mFonts.get(type);
    }

    public void setFontsToViews(FontType type, TextView... views) {
        if(views.length > 0) {
            for(int i=0, n= views.length; i<n; i++) {
                views[i].setTypeface(getFont(type));
            }
        }
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
    public void loadFonts(Context context) {
        if(mFonts == null) {
            mFonts = new EnumMap<>(FontType.class);
            mFonts.put(FontType.Regular, Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica.ttf"));
            mFonts.put(FontType.Bold, Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica-Bold.ttf"));
            mFonts.put(FontType.BoldItalic, Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica-BoldOblique.ttf"));
            mFonts.put(FontType.Light, Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica-Light.ttf"));
            mFonts.put(FontType.LightItalic, Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica-LightOblique.ttf"));
            mFonts.put(FontType.Italic, Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica-Oblique.ttf"));
        }
    }

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
