package com.whitefly.plutocrat.helpers;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.APIConnectionException;
import com.whitefly.plutocrat.models.BuyoutModel;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.TargetModel;
import com.whitefly.plutocrat.models.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import okhttp3.Headers;
import okhttp3.Response;

/**
 * Created by Satjapot on 5/9/16 AD.
 * this class manage access token to talk with server.
 */
public class SessionManager {
    public static final String PREFKEY_SESSION = "com.whitefly.plutocrat.prefs.session";
    public static final String PREFKEY_SESSION_PLUTOCRAT = "com.whitefly.plutocrat.prefs.session.plutocrat";

    // Attributes
    public String access_token;
    public String token_type = "Bearer";
    public String client;
    public long expiry = 0L;
    public String uid;
    public int user_id = 0;

    private UserModel mActiveUser = null;

    private TargetModel mPlutocrat, mInitiatingUser, mTerminalUser;

    // Getter Methods
    public UserModel getActiveUser() {
        return mActiveUser;
    }

    public TargetModel getPlutocrat() {
        return mPlutocrat;
    }

    public TargetModel getInitiatingUser() {
        return mInitiatingUser;
    }

    public void updateInitiatingUser(TargetModel model) {
        mInitiatingUser = model;
    }

    public TargetModel getTerminalUser() {
        return mTerminalUser;
    }

    public void updateTerminalUser(TargetModel model) {
        mTerminalUser = model;
    }

    public void setPlutocrat(TargetModel plutocrat){
        mPlutocrat = plutocrat;
    }

    // Methods

    /**
     * Check user status and save user data to session.
     * @return true if user is logging in and access token is valid.
     */
    public boolean isLogin(HttpClient http) {
        boolean result = false;
        Gson gson = AppPreference.getInstance().getGson();

        if(AppPreference.getInstance().getSharedPreference().contains(PREFKEY_SESSION)) {
            Headers headers = this.getHeaders();
            try {
                String strBody = http.header(headers)
                        .get(String.format(http.getContext().getString(R.string.api_profile), user_id));

                JSONObject body = new JSONObject(strBody);
                UserModel activeUser = gson.fromJson(body.getString("user"), UserModel.class);

                JSONObject userJson = body.getJSONObject("user");
                if(! userJson.isNull("active_inbound_buyout")) {
                    String inboundBuyout = userJson.getString("active_inbound_buyout");
                    activeUser.activeInboundBuyout = gson.fromJson(inboundBuyout, BuyoutModel.class);

                    String initiatingUser = http.header(headers)
                            .get(String.format(http.getContext().getString(R.string.api_profile),
                                    activeUser.activeInboundBuyout.initiatingUserId));

                    JSONObject initiatingUserJSON = new JSONObject(initiatingUser);
                    mInitiatingUser = gson.fromJson(initiatingUserJSON.getString("user"), TargetModel.class);
                }

                if(! userJson.isNull("terminal_buyout")) {
                    String inboundBuyout = userJson.getString("terminal_buyout");
                    activeUser.terminalBuyout = gson.fromJson(inboundBuyout, BuyoutModel.class);

                    String terminalUser = http.header(headers)
                            .get(String.format(http.getContext().getString(R.string.api_profile),
                                    activeUser.terminalBuyout.initiatingUserId));

                    JSONObject terminalUserJSON = new JSONObject(terminalUser);
                    mTerminalUser = gson.fromJson(terminalUserJSON.getString("user"), TargetModel.class);
                }

                updateActiveUser(activeUser);
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (APIConnectionException e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    /**
     * To update activer user data when get response from API
     * @param model UserModel
     */
    public void updateActiveUser(UserModel model) {
        mActiveUser = model;
    }

    public void save(Headers headers, int userId) {
        access_token = headers.get("Access-token");
        token_type = headers.get("Token-type");
        client = headers.get("Client");
        expiry = Long.parseLong(headers.get("Expiry"));
        uid = headers.get("Uid");
        user_id = userId;

        AppPreference.getInstance().savePrefs(PREFKEY_SESSION, this, SessionManager.class);
    }

    public void savePlutocrat(TargetModel model) {
        Gson gson = AppPreference.getInstance().getGson();

        if(model == null) {
            mPlutocrat = model;
            AppPreference.getInstance().getSharedPreference().edit()
                    .remove(PREFKEY_SESSION_PLUTOCRAT)
                    .commit();
        } else {
            if(mPlutocrat == null || mPlutocrat.id != model.id) {
                mPlutocrat = model;
                String json = gson.toJson(model);
                AppPreference.getInstance().getSharedPreference().edit()
                        .putString(PREFKEY_SESSION_PLUTOCRAT, json)
                        .apply();
            }
        }
    }

    public void loadPlutocrat() {
        Gson gson = AppPreference.getInstance().getGson();

        if(AppPreference.getInstance().getSharedPreference().contains(PREFKEY_SESSION_PLUTOCRAT)) {
            String json = AppPreference.getInstance().getSharedPreference().getString(PREFKEY_SESSION_PLUTOCRAT, null);
            mPlutocrat = gson.fromJson(json, TargetModel.class);
        }
    }

    public Headers getHeaders() {
        Headers result = null;

        // Load if necessary
        if(user_id == 0) {
            SessionManager loadedModel = AppPreference.getInstance().loadPrefs(PREFKEY_SESSION, SessionManager.class);
            access_token = loadedModel.access_token;
            token_type = loadedModel.token_type;
            client = loadedModel.client;
            expiry = loadedModel.expiry;
            uid = loadedModel.uid;
            user_id = loadedModel.user_id;
        }

        // Create Header
        result = new Headers.Builder()
                .add("access-token", access_token)
                .add("token_type", token_type)
                .add("client", client)
                .add("expiry", String.valueOf(expiry))
                .add("uid", uid)
                .build();

        return result;
    }

    public void destroy() {
        // Destroy shared preference
        AppPreference.getInstance().getSharedPreference().edit().remove(PREFKEY_SESSION).commit();

        // Clear value
        access_token = "";
        token_type = "";
        client = "";
        expiry = 0L;
        uid = "";
        mActiveUser = null;
    }
}
