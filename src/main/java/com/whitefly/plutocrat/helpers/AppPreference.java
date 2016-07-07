package com.whitefly.plutocrat.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.whitefly.plutocrat.models.TargetModel;
import com.whitefly.plutocrat.models.UserModel;
import com.whitefly.plutocrat.models.UserPersistenceModel;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;

/**
 * Created by Satjapot on 5/9/16 AD.
 * This class will keep all persistence data to use in this app.
 */
public class AppPreference {
    public static final String DEBUG_APP = "Plutocrat";
    public static final String PLATFORM = "android";

    public static final String PREF_NAME = "com.whitefly.plutocat.prefs";
    public static final String PREFKEY_USER_PERSISTENCE = "com.whitefly.plutocrat.prefs.user_persistence";
    public static final String PREFKEY_LAST_LOGIN_ID = "com.whitefly.plutocrat.prefs.last_login_id";
    public static final String PREFKEY_FCM_TOKEN = "com.whitefly.plutocrat.prefs.fcm_token";

    private static final String INSTANCE_STATE_LAST_LOGIN_ID = "com.whitefly.plutocrat.instance.login_id";
    private static final String INSTANCE_STATE_CURRENT_TARGET = "com.whitefly.plutocrat.instance.current_target";

    private static final int NO_USER_ID = 0;

    public enum FontType {
        Regular, Bold, Italic, BoldItalic, Light, LightItalic
    }

    // Attributes
    private static AppPreference singleton;
    private Gson mGson;

    private Context mContext;
    private SharedPreferences mPrefs;
    private SessionManager mSession;

    private EnumMap<FontType, Typeface> mFonts;

    private TargetModel mCurrentTarget;

    private int mLastLoginId;
    private HashMap<String, UserPersistenceModel> mUserPersistences;

    // Get/Set Methods
    public Gson getGson() {
        return mGson;
    }

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
        if(mFonts == null) {
            loadFonts(mContext);
        }
        return mFonts.get(type);
    }

    public void setFontsToViews(FontType type, TextView... views) {
        if(views.length > 0) {
            for(int i=0, n= views.length; i<n; i++) {
                views[i].setTypeface(getFont(type));
            }
        }
    }

    public UserPersistenceModel getCurrentUserPersistence() {
        int currentUserId = mSession.user_id;
        if(currentUserId == NO_USER_ID) {
            currentUserId = mLastLoginId;
        }

        String strCurrentUserId = String.valueOf(currentUserId);
        UserPersistenceModel model = null;
        if(mUserPersistences.containsKey(strCurrentUserId)) {
            model = mUserPersistences.get(strCurrentUserId);
        } else {
            if(currentUserId != NO_USER_ID) {
                model = new UserPersistenceModel();
                model.userId = currentUserId;
                model.noticeId = UserModel.NOTICE_GETTING_STARTED;
                model.email = mSession.uid;
                mUserPersistences.put(strCurrentUserId, model);
                saveUserPersistence();
            }
        }
        return model;
    }

    // Constructor
    public AppPreference(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(PREF_NAME, 0);
        mSession = new SessionManager();
        mGson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new GSONUTCDateAdapter())
                .create();

        loadUserPersistence();
    }

    // Static Methods
    public static AppPreference createInstance(Context context) {
        singleton = new AppPreference(context);
        return singleton;
    }

    public static AppPreference getInstance() {
        return singleton;
    }

    // Methods
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_STATE_LAST_LOGIN_ID, mLastLoginId);
        if(mCurrentTarget != null) {
            outState.putString(INSTANCE_STATE_CURRENT_TARGET, mGson.toJson(mCurrentTarget));
        }

        mSession.onSaveInstanceState(outState);
    }

    public void onLoadInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            mLastLoginId = savedInstanceState.getInt(INSTANCE_STATE_LAST_LOGIN_ID);
            if(savedInstanceState.containsKey(INSTANCE_STATE_CURRENT_TARGET)) {
                mCurrentTarget = mGson.fromJson(savedInstanceState.getString(
                        INSTANCE_STATE_CURRENT_TARGET), TargetModel.class);
            }

            mSession.onLoadInstanceState(savedInstanceState);
        }
    }

    public void loadUserPersistence() {
        mLastLoginId = mPrefs.getInt(PREFKEY_LAST_LOGIN_ID, NO_USER_ID);
        mUserPersistences = new HashMap<>();
        if(mPrefs.contains(PREFKEY_USER_PERSISTENCE)) {
            HashMap<String, String> userJSONPersistence = new HashMap<>();
            userJSONPersistence = loadPrefs(PREFKEY_USER_PERSISTENCE, userJSONPersistence.getClass());

            for(String key : userJSONPersistence.keySet()) {
                mUserPersistences.put(key, mGson.fromJson(userJSONPersistence.get(key), UserPersistenceModel.class));
            }
        }
    }

    public void saveUserPersistence() {
        HashMap<String, String> userJSONPersistence = new HashMap<>();
        for (String key : mUserPersistences.keySet()) {
            userJSONPersistence.put(key, mGson.toJson(mUserPersistences.get(key)));
        }
        savePrefs(PREFKEY_USER_PERSISTENCE, userJSONPersistence, HashMap.class);
    }

    public void saveLastLoginId(int id) {
        mLastLoginId = id;
        mPrefs.edit().putInt(PREFKEY_LAST_LOGIN_ID, id).apply();
    }

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
                .apply();
    }

    public <T extends Object> T loadPrefs(String key, Class<T> classType) {
        String json = mPrefs.getString(key, "{}");
        T result = mGson.fromJson(json, classType);
        return result;
    }
}
