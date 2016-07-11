package com.whitefly.plutocrat;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.mainmenu.MainMenuActivity;
import com.whitefly.plutocrat.login.LoginActivity;
import com.whitefly.plutocrat.splash.events.LoadUserDataEvent;
import com.whitefly.plutocrat.splash.presenters.SplashPresenter;
import com.whitefly.plutocrat.splash.views.ISplashView;

public class MainActivity extends AppCompatActivity implements ISplashView {
    private static final int TIME_SHOW_SPLASH_SCREEN = 2000;

    // Attributes
    private SplashPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize
        AppPreference.getInstance().loadFonts(this);
        if(presenter == null) {
            presenter = new SplashPresenter(this, this);
            EventBus.getInstance().register(presenter);
        }

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(AppPreference.DEBUG_APP, "Refreshed token: " + refreshedToken);

        // Go to next page for a period
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getInstance().post(new LoadUserDataEvent());
            }
        }, TIME_SHOW_SPLASH_SCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().unregister(presenter);
    }

    /*
    Implement View
     */
    @Override
    public void loadActivity(boolean isLogin) {
        Intent intent;
        if(isLogin) {
            intent = new Intent(this, MainMenuActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
