package com.whitefly.plutocrat.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.whitefly.plutocrat.helpers.AppPreference;

/**
 * Created by Satjapot on 6/4/16 AD.
 */
public class CloudMessageInstantIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(AppPreference.DEBUG_APP, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
    }
}
