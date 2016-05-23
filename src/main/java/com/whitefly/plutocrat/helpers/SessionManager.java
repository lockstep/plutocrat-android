package com.whitefly.plutocrat.helpers;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Response;

/**
 * Created by Satjapot on 5/9/16 AD.
 * this class manage access token to talk with server.
 */
public class SessionManager {
    public static final String PREFKEY_SESSION = "com.whitefly.plutocrat.prefs.session";

    // Attributes
    public String access_token;
    public String token_type = "Bearer";
    public String client;
    public long expiry = 0L;
    public String uid;
    public int user_id = 0;

    private UserModel mActiveUser = null;

    // Getter Methods
    public UserModel getmActiveUser() {
        return mActiveUser;
    }

    // Methods

    /**
     * Check user status and save user data to session.
     * @return true if user is logging in and access token is valid.
     */
    public boolean isLogin(HttpClient http) {
        boolean result = false;

        if(AppPreference.getInstance().getSharedPreference().contains(PREFKEY_SESSION)) {
            Headers headers = this.getHeaders();
            try {
                String strBody = http.header(headers).get(
                        String.format(http.getContext().getString(R.string.api_profile), user_id)
                );

                Log.d(AppPreference.DEBUG_APP, String.format("Response: %s", strBody));
                JSONObject body = new JSONObject(strBody);

                // Get error
                if(! body.isNull("meta")) {
                    MetaModel model = new MetaModel(body.getJSONObject("meta"));
                    if(model.isError()) {
                        throw new Exception("Session expired.");
                    }
                }

                // Save active user
                Gson gson = new Gson();
                updateActiveUser(gson.fromJson(body.getString("user"), UserModel.class));
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
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
