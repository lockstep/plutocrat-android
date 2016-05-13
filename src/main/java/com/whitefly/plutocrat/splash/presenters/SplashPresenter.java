package com.whitefly.plutocrat.splash.presenters;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.otto.Subscribe;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.HttpClient;
import com.whitefly.plutocrat.splash.events.LoadUserDataEvent;
import com.whitefly.plutocrat.splash.views.ISplashView;

import org.json.JSONObject;

/**
 * Created by Satjapot on 5/12/16 AD.
 */
public class SplashPresenter {

    // Attributes
    private Context mContext;
    private HttpClient mHttp;

    private ISplashView mSplashView;

    // Constructor
    public SplashPresenter(Context context, ISplashView splashView) {
        mContext = context;
        mHttp = new HttpClient(context);

        mSplashView = splashView;
    }

    // Event Handler
    @Subscribe
    public void onLoadingUserData(LoadUserDataEvent event) {
        Log.d(AppPreference.DEBUG_APP, "Checking last log in...");
        new LoadUserDataCallback().execute();
    }

    /*
    Request Callback
     */
    private class LoadUserDataCallback extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(AppPreference.DEBUG_APP, "Request api server.");
            return AppPreference.getInstance().getSession().isLogin(new HttpClient(mContext));
        }

        @Override
        protected void onPostExecute(Boolean b) {
            // Go to appropriate activity
            Log.d(AppPreference.DEBUG_APP, "Request complete. Go to next activity.");
            mSplashView.loadActivity(b);
        }
    }
}
