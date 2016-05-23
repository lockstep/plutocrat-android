package com.whitefly.plutocrat.login;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.mainmenu.MainMenuActivity;
import com.whitefly.plutocrat.login.fragments.LoginFragment;
import com.whitefly.plutocrat.login.fragments.ResetPassword1Fragment;
import com.whitefly.plutocrat.login.fragments.ResetPassword2Fragment;
import com.whitefly.plutocrat.login.presenters.LoginPresenter;
import com.whitefly.plutocrat.login.views.ILoginMainView;
import com.whitefly.plutocrat.login.views.ILoginView;

public class LoginActivity extends AppCompatActivity implements ILoginMainView {

    // Attributes
    private LoginPresenter presenter;
    private Intent mMainMenuIntent;

    // Fragments
    private Fragment mLoginFragment, mReset1Fragment, mReset2Fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Create Fragments
        mLoginFragment = LoginFragment.newInstance();
        mReset1Fragment = ResetPassword1Fragment.newInstance();
        mReset2Fragment = ResetPassword2Fragment.newInstance();

        // Add Fragments Transaction
        getSupportFragmentManager().beginTransaction()
                .add(R.id.rlo_login_main, mLoginFragment)
                .commit();

        // Initialize
        if(presenter == null) {
            presenter = new LoginPresenter(this, this, (ILoginView) mLoginFragment);
        }
        mMainMenuIntent = new Intent(this, MainMenuActivity.class);
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
    Implement ILoginMain View
     */
    @Override
    public void goToResetPassword1() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right, R.anim.slide_out_left,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right
                )
                .replace(R.id.rlo_login_main, mReset1Fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoResetPassword2() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right, R.anim.slide_out_left,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right
                )
                .replace(R.id.rlo_login_main, mReset2Fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void backToLogin() {
        // Return to login fragment
        FragmentManager manager = getSupportFragmentManager();
        for(int i=0, n=manager.getBackStackEntryCount(); i<n; i++) {
            manager.popBackStack();
        }
    }

    @Override
    public void goToMainMenu() {
        startActivity(mMainMenuIntent);

        finish();
    }
}
