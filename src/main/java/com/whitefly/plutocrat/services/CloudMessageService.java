package com.whitefly.plutocrat.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.APIConnectionException;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.HttpClient;
import com.whitefly.plutocrat.mainmenu.events.LoadBuyoutsEvent;
import com.whitefly.plutocrat.mainmenu.events.LoadTargetsEvent;
import com.whitefly.plutocrat.mainmenu.events.SetHomeStateEvent;
import com.whitefly.plutocrat.models.UserModel;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.Headers;

/**
 * Created by Satjapot on 6/4/16 AD.
 */
public class CloudMessageService extends FirebaseMessagingService {
    private static final int PER_PAGE = 15;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        HttpClient http = new HttpClient(getApplicationContext());
        Headers headers = AppPreference.getInstance().getSession().getHeaders();
        UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
        int userId = 0;
        if(activeUser == null) {
            userId = AppPreference.getInstance().getCurrentUserPersistence().userId;
        } else {
            userId = activeUser.id;
        }
        String requestUrl = String.format(getString(R.string.api_profile), userId);

        try {
            Log.d(AppPreference.DEBUG_APP, "FCM Send request");
            String response = http.header(headers).get(requestUrl);
            AppPreference.getInstance().getSession().updateUserJson(response);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    EventBus.getInstance().post(new SetHomeStateEvent());
                    EventBus.getInstance().post(new LoadTargetsEvent(1, PER_PAGE));
                    EventBus.getInstance().post(new LoadBuyoutsEvent(1, PER_PAGE));
                }
            });

        } catch (IOException | APIConnectionException | JSONException e) {
            e.printStackTrace();
        }
    }
}
