package com.whitefly.plutocrat.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.APIConnectionException;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.HttpClient;
import com.whitefly.plutocrat.models.UserModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;

/**
 * Created by Satjapot on 6/4/16 AD.
 */
public class CloudMessageInstantIdService extends FirebaseInstanceIdService {
    public static final String FIELD_NAME_TOKEN = "token";
    public static final String FIELD_NAME_PLATFORM = "platform";
    public static final String FIELD_NAME_DEVICE_ATTRIBUTES = "devices_attributes";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(AppPreference.DEBUG_APP, "Refreshed token: " + refreshedToken);

        SharedPreferences shared = AppPreference.createInstance(getApplicationContext()).getSharedPreference();
        shared.edit().putString(AppPreference.PREFKEY_FCM_TOKEN, refreshedToken).apply();
    }
}
