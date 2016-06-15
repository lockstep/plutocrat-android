package com.whitefly.plutocrat.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.whitefly.plutocrat.helpers.AppPreference;

/**
 * Created by Satjapot on 6/4/16 AD.
 */
public class CloudMessageService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(AppPreference.DEBUG_APP, "FCM DATA: " + remoteMessage.getData().toString());
    }
}
