package com.whitefly.plutocrat.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.mainmenu.MainMenuActivity;
import com.whitefly.plutocrat.login.fragments.LoginFragment;
import com.whitefly.plutocrat.login.fragments.ResetPassword1Fragment;
import com.whitefly.plutocrat.login.fragments.ResetPassword2Fragment;
import com.whitefly.plutocrat.login.presenters.LoginPresenter;
import com.whitefly.plutocrat.login.views.ILoginMainView;
import com.whitefly.plutocrat.login.views.ILoginView;

public class LoginActivity extends AppCompatActivity implements ILoginMainView {
    public static final String BUNDLE_INITIATE_LOGIN_STATE = "com.whitefly.plutocrat.bundle.initiateLogin";

    // Attributes
    private LoginPresenter presenter;
    private Intent mMainMenuIntent;

    private AlertDialog mLoadingDialog;
    private TextView mTvLoadingMessage;

    // Fragments
    private Fragment mLoginFragment, mReset1Fragment, mReset2Fragment;

    // Methods
    private void createLoadingDialog() {
        View root = getLayoutInflater().inflate(R.layout.dialog_loading, null, false);
        mTvLoadingMessage = (TextView) root.findViewById(R.id.tv_loading_message);

        mLoadingDialog = new AlertDialog.Builder(this)
                .setView(root)
                .setCancelable(false)
                .create();
        mLoadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ILoginView.ViewState loginState =
                (ILoginView.ViewState) getIntent().getSerializableExtra(BUNDLE_INITIATE_LOGIN_STATE);
        if(loginState == null) {
            loginState = ILoginView.ViewState.Register;
        }

        // Create Fragments
        mLoginFragment = LoginFragment.newInstance(loginState);
        mReset1Fragment = ResetPassword1Fragment.newInstance();
        mReset2Fragment = ResetPassword2Fragment.newInstance();

        // Add Fragments Transaction
        getSupportFragmentManager().beginTransaction()
                .add(R.id.rlo_login_main, mLoginFragment)
                .commit();

        createLoadingDialog();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoadingDialog != null) mLoadingDialog.dismiss();
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

    @Override
    public void handleLoadingDialog(boolean isShow) {
        if(isShow) {
            mLoadingDialog.show();
        } else {
            mLoadingDialog.hide();
        }
    }

    @Override
    public void handleError(String title, String message) {

        Typeface font = AppPreference.getInstance().getFont(AppPreference.FontType.Regular);

        SpannableString spanTitle = new SpannableString(title);
        spanTitle.setSpan(font, 0, spanTitle.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString spanMessage = new SpannableString(message);
        spanMessage.setSpan(font, 0, spanMessage.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString negativeText = new SpannableString(getString(R.string.caption_close));
        negativeText.setSpan(font, 0, negativeText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        new AlertDialog.Builder(this)
                .setTitle(spanTitle)
                .setMessage(spanMessage)
                .setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
