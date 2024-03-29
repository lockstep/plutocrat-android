package com.whitefly.plutocrat.helpers;

import android.os.Bundle;

import com.google.gson.Gson;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.APIConnectionException;
import com.whitefly.plutocrat.models.BuyoutModel;
import com.whitefly.plutocrat.models.TargetModel;
import com.whitefly.plutocrat.models.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;

/**
 * Created by Satjapot on 5/9/16 AD.
 * this class manage access token to talk with server.
 */
public class SessionManager {
    public static final String PREFKEY_SESSION = "com.whitefly.plutocrat.prefs.session";
    public static final String PREFKEY_SESSION_PLUTOCRAT = "com.whitefly.plutocrat.prefs.session.plutocrat";

    private static final String INSTANCE_STATE_ACTIVE_USER = "com.whitefly.plutocrat.instance.active_user";
    private static final String INSTANCE_STATE_ACTIVE_USER_INITIATING_BUYOUT = "com.whitefly.plutocrat.instance.active_user_initiating_buyout";
    private static final String INSTANCE_STATE_ACTIVE_USER_TERMINAL_BUYOUT = "com.whitefly.plutocrat.instance.active_user_terminal_buyout";
    private static final String INSTANCE_STATE_INITIATING_TARGET = "com.whitefly.plutocrat.instance.initiating_user";
    private static final String INSTANCE_STATE_TERMINAL_TARGET = "com.whitefly.plutocrat.instance.terminal_user";

    // Attributes
    public String access_token;
    public String token_type = "Bearer";
    public String client;
    public long expiry = 0L;
    public String uid;
    public int user_id = 0;

    private transient UserModel mActiveUser = null;

    private transient TargetModel mPlutocrat, mInitiatingUser, mTerminalUser;

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
    public void updateUserJson(String userResponse)
            throws JSONException, IOException, APIConnectionException {
        Gson gson = AppPreference.getInstance().getGson();

        JSONObject body = new JSONObject(userResponse);
        UserModel activeUser = this.mActiveUser;
        if(body.getJSONObject("user").has("email")) {
            activeUser =  gson.fromJson(body.getString("user"), UserModel.class);
        } else {
            TargetModel userModel = gson.fromJson(body.getString("user"), TargetModel.class);
            activeUser.updateTargetData(userModel);
        }

        JSONObject userJson = body.getJSONObject("user");
        if(userJson.isNull("active_inbound_buyout")) {
            activeUser.activeInboundBuyout = null;
        } else {
            String inboundBuyout = userJson.getString("active_inbound_buyout");
            activeUser.activeInboundBuyout = gson.fromJson(inboundBuyout, BuyoutModel.class);

            String initiatingUserString = userJson.getJSONObject("active_inbound_buyout").getString("initiating_user");
            String targetUserString = userJson.getJSONObject("active_inbound_buyout").getString("target_user");
            activeUser.activeInboundBuyout.initiatingUser = gson.fromJson(initiatingUserString, TargetModel.class);
            activeUser.activeInboundBuyout.targetUser = gson.fromJson(targetUserString, TargetModel.class);
            mInitiatingUser = gson.fromJson(initiatingUserString, TargetModel.class);
        }

        if(userJson.isNull("terminal_buyout")) {
            activeUser.terminalBuyout = null;
        } else {
            String inboundBuyout = userJson.getString("terminal_buyout");
            activeUser.terminalBuyout = gson.fromJson(inboundBuyout, BuyoutModel.class);

            String initiatingUserString = userJson.getJSONObject("terminal_buyout").getString("initiating_user");
            String targetUserString = userJson.getJSONObject("terminal_buyout").getString("target_user");
            activeUser.terminalBuyout.initiatingUser = gson.fromJson(initiatingUserString, TargetModel.class);
            activeUser.terminalBuyout.targetUser = gson.fromJson(targetUserString, TargetModel.class);
            mTerminalUser = gson.fromJson(initiatingUserString, TargetModel.class);
        }

        updateActiveUser(activeUser);
    }

    /**
     * Check user status and save user data to session.
     * @return true if user is logging in and access token is valid.
     */
    public boolean isLogin(HttpClient http) {
        boolean result = false;

        if(AppPreference.getInstance().getSharedPreference().contains(PREFKEY_SESSION)) {
            Headers headers = this.getHeaders();
            try {
                String strBody = http.header(headers)
                        .get(String.format(http.getContext().getString(R.string.api_profile), user_id));

                updateUserJson(strBody);

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
            mPlutocrat = model;
            String json = gson.toJson(model);
            AppPreference.getInstance().getSharedPreference().edit()
                    .putString(PREFKEY_SESSION_PLUTOCRAT, json)
                    .apply();
        }
    }

    public void loadPlutocrat() {
        Gson gson = AppPreference.getInstance().getGson();

        if(AppPreference.getInstance().getSharedPreference().contains(PREFKEY_SESSION_PLUTOCRAT)) {
            String json = AppPreference.getInstance().getSharedPreference().getString(PREFKEY_SESSION_PLUTOCRAT, null);
            mPlutocrat = gson.fromJson(json, TargetModel.class);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        Gson gson = AppPreference.getInstance().getGson();
        if(mActiveUser != null) {
            outState.putString(INSTANCE_STATE_ACTIVE_USER, gson.toJson(mActiveUser));
            if(mActiveUser.activeInboundBuyout != null) {
                outState.putString(INSTANCE_STATE_ACTIVE_USER_INITIATING_BUYOUT, gson.toJson(mActiveUser.activeInboundBuyout));
                outState.putString(INSTANCE_STATE_INITIATING_TARGET, gson.toJson(mInitiatingUser));
            }
            if(mActiveUser.terminalBuyout != null) {
                outState.putString(INSTANCE_STATE_ACTIVE_USER_TERMINAL_BUYOUT, gson.toJson(mActiveUser.terminalBuyout));
                outState.putString(INSTANCE_STATE_TERMINAL_TARGET, gson.toJson(mTerminalUser));
            }
        }
    }

    public void onLoadInstanceState(Bundle savedInstanceState) {
        Gson gson = AppPreference.getInstance().getGson();
        if(savedInstanceState.containsKey(INSTANCE_STATE_ACTIVE_USER)) {
            mActiveUser = gson.fromJson(savedInstanceState.getString(INSTANCE_STATE_ACTIVE_USER), UserModel.class);
            if(savedInstanceState.containsKey(INSTANCE_STATE_ACTIVE_USER_INITIATING_BUYOUT)) {
                mActiveUser.activeInboundBuyout =
                        gson.fromJson(savedInstanceState.getString(INSTANCE_STATE_ACTIVE_USER_INITIATING_BUYOUT), BuyoutModel.class);
                mInitiatingUser = gson.fromJson(savedInstanceState.getString(INSTANCE_STATE_INITIATING_TARGET), TargetModel.class);
            }
            if(savedInstanceState.containsKey(INSTANCE_STATE_ACTIVE_USER_TERMINAL_BUYOUT)) {
                mActiveUser.terminalBuyout =
                        gson.fromJson(savedInstanceState.getString(INSTANCE_STATE_ACTIVE_USER_TERMINAL_BUYOUT), BuyoutModel.class);
                mTerminalUser = gson.fromJson(savedInstanceState.getString(INSTANCE_STATE_TERMINAL_TARGET), TargetModel.class);
            }
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
        AppPreference.getInstance().saveLastLoginId(user_id);

        // Clear value
        access_token = "";
        token_type = "";
        client = "";
        expiry = 0L;
        uid = "";
        mActiveUser = null;
    }
}
