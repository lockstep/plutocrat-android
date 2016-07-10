package com.whitefly.plutocrat.login.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.FormValidationException;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.FormValidationHelper;
import com.whitefly.plutocrat.login.events.BackToLoginEvent;
import com.whitefly.plutocrat.login.events.ResetPassword1ErrorEvent;
import com.whitefly.plutocrat.login.events.ResetPassword2ErrorEvent;
import com.whitefly.plutocrat.login.events.ResetPasswordEvent;
import com.whitefly.plutocrat.login.views.ILoginView;
import com.whitefly.plutocrat.models.MetaModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResetPassword2Fragment extends Fragment {
    private static final String FORM_EMAIL = "email";
    private static final String FORM_RESET_PASSWORD_TOKEN = "reset_password_token";
    private static final String FORM_NEW_PASSWORD = "password";
    private static final String FORM_CONFIRM_PASSWORD = "password_confirmation";

    // Attributes
    private FormValidationHelper mFormValidator;

    // Views
    private TextView mTvTitle, mTvContent, mTvResetCaption;
    private EditText mEdtEmail, mEdtResetToken, mEdtNewPassword, mEdtConfirmPassword;
    private Button mBtnReset, mBtnLoginLink, mBtnRegisterLink;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static ResetPassword2Fragment newInstance() {
        ResetPassword2Fragment fragment = new ResetPassword2Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFormValidator = new FormValidationHelper();
        EventBus.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().unregister(this);

        mEdtEmail.setError(null);
        mEdtResetToken.setError(null);
        mEdtNewPassword.setError(null);
        mEdtConfirmPassword.setError(null);
    }

    public ResetPassword2Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get Views
        View root = inflater.inflate(R.layout.fragment_resetpw2_v2, container, false);
        mTvTitle        = (TextView) root.findViewById(R.id.tv_reset2_title);
        mTvContent      = (TextView) root.findViewById(R.id.tv_reset2_content);
        mTvResetCaption = (TextView) root.findViewById(R.id.tv_reset2_caption);
        mBtnLoginLink   = (Button) root.findViewById(R.id.btn_reset2_login_link);
        mBtnRegisterLink = (Button) root.findViewById(R.id.btn_reset2_register_link);
        mEdtEmail       = (EditText) root.findViewById(R.id.edt_reset2_email);
        mEdtResetToken  = (EditText) root.findViewById(R.id.edt_reset2_token);
        mEdtNewPassword = (EditText) root.findViewById(R.id.edt_reset2_newpw);
        mEdtConfirmPassword = (EditText) root.findViewById(R.id.edt_reset2_confirmpw);
        mBtnReset       = (Button) root.findViewById(R.id.btn_reset2_reset);

        // Initiate
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                mTvTitle, mTvContent, mTvResetCaption, mEdtEmail, mEdtResetToken, mEdtNewPassword,
                mEdtConfirmPassword);
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Bold,
                mBtnLoginLink, mBtnRegisterLink, mBtnReset);

        mFormValidator.addView(FORM_RESET_PASSWORD_TOKEN, "Reset Token", mEdtResetToken);
        mFormValidator.addView(FORM_NEW_PASSWORD, "New Password", mEdtNewPassword);

        // Event Handler
        mBtnLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getInstance().post(new BackToLoginEvent(ILoginView.ViewState.Login));
            }
        });
        mBtnRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getInstance().post(new BackToLoginEvent(ILoginView.ViewState.Register));
            }
        });
        mBtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager imm =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = mEdtEmail.getText().toString().trim();
                String token = mEdtResetToken.getText().toString().trim();
                String newPassword = mEdtNewPassword.getText().toString();
                String confirmPassword = mEdtConfirmPassword.getText().toString();

                EventBus.getInstance().post(new ResetPasswordEvent(email, token, newPassword, confirmPassword));
            }
        });
        mEdtNewPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    mBtnReset.performClick();
                    return true;
                }
                return false;
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        mEdtEmail.setText("");
        mEdtResetToken.setText("");
        mEdtNewPassword.setText("");
        mEdtConfirmPassword.setText("");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Subscribe
    public void onResetPassword2Error(ResetPassword2ErrorEvent event) {
        MetaModel meta = event.getMetaModel();
        try {
            FormValidationHelper.ValidationRule rules = mFormValidator.begin();
            for(String key : meta.getKeys()) {
                if(!key.equals("full_messages")) {
                    rules.ruleRaiseError(key, meta.getValue(key));
                }
            }
            rules.validate();
        } catch (FormValidationException e) {
            e.printStackTrace();

            for(FormValidationException.ValidationItem item : e.getItems()) {
                item.raiseError();
            }
            if(e.getItems().size() > 0) {
                e.getItems().get(0).getView().requestFocus();
            }
        }
    }
}
