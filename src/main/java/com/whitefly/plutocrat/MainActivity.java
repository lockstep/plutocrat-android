package com.whitefly.plutocrat;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.mainmenu.MainMenuActivity;
import com.whitefly.plutocrat.login.LoginActivity;
import com.whitefly.plutocrat.splash.events.LoadUserDataEvent;
import com.whitefly.plutocrat.splash.presenters.SplashPresenter;
import com.whitefly.plutocrat.splash.views.ISplashView;

public class MainActivity extends AppCompatActivity implements ISplashView {

    // Attributes
    private SplashPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize
        AppPreference.createInstance(this);
        presenter = new SplashPresenter(this, this);

        // Go to next page for a period
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getInstance().post(new LoadUserDataEvent());
            }
        }, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getInstance().register(presenter);
    }

    @Override
    protected void onPause() {
        super.onPause();
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