package com.whitefly.plutocrat.login.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.exception.FormValidationException;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.FormValidationHelper;
import com.whitefly.plutocrat.login.LoginActivity;
import com.whitefly.plutocrat.login.events.ForgotPasswordEvent;
import com.whitefly.plutocrat.login.events.RegisterEvent;
import com.whitefly.plutocrat.login.events.SignInEvent;
import com.whitefly.plutocrat.login.views.ILoginView;
import com.whitefly.plutocrat.models.MetaModel;
import com.whitefly.plutocrat.models.UserPersistenceModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment implements ILoginView, View.OnClickListener {
    private static final String FRAGMENT_WEB_VIEW = "frg_web_view";
    private static final String VALIDATION_PREFIX_REGISTER = "_register";
    private static final String VALIDATION_PREFIX_SIGNIN = "_sign_in";

    // Attributes
    private ILoginView.ViewState mCurrentState;
    private FormValidationHelper mFormValidator;
    private String mPrefixValidation;

    // Views
    private TextView mTvWelcomeTitle, mTvWelcomeContent, mTvSignInTitle, mTvRegisterTitile;
    private Button mBtnLoginLink, mBtnRegisterLink, mBtnForgotPwLink, mBtnEula, mBtnPrivacy;
    private RelativeLayout mRloRegister, mRloSignin, mRloRegisterButtonGroup;
    private LinearLayout mLloSignInButtonGroup;
    private Button mBtnRegister, mBtnSigin;
    private EditText mEdtRegDisplayName, mEdtRegEmail, mEdtRegPassword;
    private EditText mEdtSignInEmail, mEdtSignInPassword;

    private WebViewFragment mFrgWebView;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance(ILoginView.ViewState initiateState) {
        LoginFragment fragment = new LoginFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(LoginActivity.BUNDLE_INITIATE_LOGIN_STATE, initiateState);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFormValidator = new FormValidationHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        // Get views
        mTvWelcomeTitle     = (TextView) root.findViewById(R.id.tv_welcome_title);
        mTvSignInTitle      = (TextView) root.findViewById(R.id.tv_signin_title);
        mTvRegisterTitile   = (TextView) root.findViewById(R.id.tv_register_title);
        mTvWelcomeContent   = (TextView) root.findViewById(R.id.tv_welcome_content);
        mBtnLoginLink       = (Button) root.findViewById(R.id.btn_login_link);
        mBtnRegisterLink    = (Button) root.findViewById(R.id.btn_register_link);
        mBtnForgotPwLink    = (Button) root.findViewById(R.id.btn_forgotpw_link);
        mRloRegister        = (RelativeLayout) root.findViewById(R.id.rlo_register);
        mRloSignin          = (RelativeLayout) root.findViewById(R.id.rlo_signin);
        mBtnRegister        = (Button) root.findViewById(R.id.btn_register);
        mBtnSigin           = (Button) root.findViewById(R.id.btn_signin);
        mEdtRegDisplayName  = (EditText) root.findViewById(R.id.edt_reg_displayname);
        mEdtRegEmail        = (EditText) root.findViewById(R.id.edt_reg_email);
        mEdtRegPassword     = (EditText) root.findViewById(R.id.edt_reg_pw);
        mEdtSignInEmail     = (EditText) root.findViewById(R.id.edt_signin_email);
        mEdtSignInPassword  = (EditText) root.findViewById(R.id.edt_signin_pw);
        mLloSignInButtonGroup = (LinearLayout) root.findViewById(R.id.llo_sign_in_button_group);
        mBtnEula            = (Button) root.findViewById(R.id.btn_eula);
        mBtnPrivacy         = (Button) root.findViewById(R.id.btn_privacy);
        mRloRegisterButtonGroup = (RelativeLayout) root.findViewById(R.id.rlo_register_button_group);

        // Initialize
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                mTvWelcomeTitle, mTvWelcomeContent, mBtnLoginLink, mBtnRegisterLink,
                mBtnForgotPwLink, mBtnRegister, mBtnSigin, mEdtRegDisplayName,
                mEdtRegEmail, mEdtRegPassword, mEdtSignInEmail, mEdtSignInPassword,
                mTvSignInTitle, mTvRegisterTitile, mBtnEula, mBtnPrivacy);

        mFormValidator.addView("display_name" + VALIDATION_PREFIX_REGISTER, "Display Name", mEdtRegDisplayName);
        mFormValidator.addView("email" + VALIDATION_PREFIX_REGISTER, "E-mail", mEdtRegEmail);
        mFormValidator.addView("password" + VALIDATION_PREFIX_REGISTER, "Password", mEdtRegPassword);
        mFormValidator.addView("email" + VALIDATION_PREFIX_SIGNIN, "E-mail", mEdtSignInEmail);
        mFormValidator.addView("password" + VALIDATION_PREFIX_SIGNIN, "Password", mEdtSignInPassword);

        mTvWelcomeContent.setText(Html.fromHtml(getString(R.string.welcome_content)));
        if(mCurrentState == null) {
            ILoginView.ViewState loginState =
                    (ILoginView.ViewState) getArguments().getSerializable(LoginActivity.BUNDLE_INITIATE_LOGIN_STATE);
            if (loginState == null) {
                mCurrentState = ViewState.Register;
            } else {
                changeState(loginState);
            }
        }

        mFrgWebView = WebViewFragment.newInstance();

        UserPersistenceModel userPersistence = AppPreference.getInstance().getCurrentUserPersistence();
        if(userPersistence != null) {
            mEdtSignInEmail.setText(userPersistence.email);
        }

        // Event Handler
        mBtnLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just show another view
                changeState(ViewState.Login);
            }
        });
        mBtnRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just show another view;
                changeState(ViewState.Register);
            }
        });
        mBtnForgotPwLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send event
                EventBus.getInstance().post(new ForgotPasswordEvent());
            }
        });
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send event
                InputMethodManager imm =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                mPrefixValidation = VALIDATION_PREFIX_REGISTER;

                String display = mEdtRegDisplayName.getText().toString();
                String email = mEdtRegEmail.getText().toString();
                String password = mEdtRegPassword.getText().toString();
                EventBus.getInstance().post(new RegisterEvent(display, email, password));
            }
        });
        mBtnSigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send event
                InputMethodManager imm =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                mPrefixValidation = VALIDATION_PREFIX_SIGNIN;

                String email = mEdtSignInEmail.getText().toString();
                String password = mEdtSignInPassword.getText().toString();
                EventBus.getInstance().post(new SignInEvent(email, password));
            }
        });
        mEdtRegPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    mBtnRegister.performClick();
                    return true;
                }
                return false;
            }
        });
        mEdtSignInPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    mBtnSigin.performClick();
                    return true;
                }
                return false;
            }
        });
        mBtnPrivacy.setOnClickListener(this);
        mBtnEula.setOnClickListener(this);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        changeState(mCurrentState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        String url = null;
        String title = null;
        if(v == mBtnEula) {
            url = getString(R.string.eula_url);
            title = getString(R.string.caption_eula);
        } else if(v == mBtnPrivacy) {
            url = getString(R.string.privacy_url);
            title = getString(R.string.caption_privacy);
        }

        if(url != null) {
            mFrgWebView.setTitle(title);
            mFrgWebView.loadUrl(url);
            mFrgWebView.show(getActivity().getFragmentManager(), FRAGMENT_WEB_VIEW);
        }
    }

    /*
    Implement ILoginView
     */
    @Override
    public void changeState(ViewState state) {
        if(state == ViewState.Login) {
            mRloRegister.setVisibility(View.GONE);
            mRloRegisterButtonGroup.setVisibility(View.GONE);
            mRloSignin.setVisibility(View.VISIBLE);
            mLloSignInButtonGroup.setVisibility(View.VISIBLE);
        } else {
            mRloRegister.setVisibility(View.VISIBLE);
            mRloRegisterButtonGroup.setVisibility(View.VISIBLE);
            mRloSignin.setVisibility(View.GONE);
            mLloSignInButtonGroup.setVisibility(View.GONE);
        }
        mCurrentState = state;
    }

    @Override
    public void toast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void handleError(MetaModel meta) {
        try {
            FormValidationHelper.ValidationRule rules = mFormValidator.begin();
            for(String key : meta.getKeys()) {
                rules.ruleRaiseError(key + mPrefixValidation, meta.getValue(key));
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
