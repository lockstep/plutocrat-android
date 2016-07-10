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
public class Login2Fragment extends Fragment implements ILoginView, View.OnClickListener {
    private static final String FRAGMENT_WEB_VIEW = "frg_web_view";
    private static final String VALIDATION_PREFIX_REGISTER = "_register";
    private static final String VALIDATION_PREFIX_SIGNIN = "_sign_in";

    // Attributes
    private ILoginView.ViewState mCurrentState;
    private FormValidationHelper mFormValidator;
    private String mPrefixValidation;

    // Views
    private RelativeLayout mRloSignIn, mRloRegister;
    private EditText mEdtSignInEmail, mEdtSignInPassword;
    private EditText mEdtRegisterDisplayName, mEdtRegisterEmail, mEdtRegisterPassword;
    private TextView mTvForgotPasswordLink, mTvAlreadyMemberLink;
    private Button mBtnSignIn, mBtnNewMember, mBtnRegister;
    private Button mBtnEula, mBtnPrivacy;

    private WebViewFragment mFrgWebView;

    public Login2Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static Login2Fragment newInstance(ILoginView.ViewState initiateState) {
        Login2Fragment fragment = new Login2Fragment();
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
        View root = inflater.inflate(R.layout.fragment_login_v2, container, false);

        // Get views
        mRloSignIn              = (RelativeLayout) root.findViewById(R.id.rlo_sign_in);
        mRloRegister            = (RelativeLayout) root.findViewById(R.id.rlo_register);
        mEdtSignInEmail         = (EditText) root.findViewById(R.id.edt_sign_in_email);
        mEdtSignInPassword      = (EditText) root.findViewById(R.id.edt_sign_in_pw);
        mEdtRegisterDisplayName = (EditText) root.findViewById(R.id.edt_reg_displayname);
        mEdtRegisterEmail       = (EditText) root.findViewById(R.id.edt_reg_email);
        mEdtRegisterPassword    = (EditText) root.findViewById(R.id.edt_reg_pw);
        mBtnSignIn              = (Button) root.findViewById(R.id.btn_sign_in);
        mBtnNewMember           = (Button) root.findViewById(R.id.btn_new_member);
        mBtnRegister            = (Button) root.findViewById(R.id.btn_register);
        mBtnEula                = (Button) root.findViewById(R.id.btn_eula);
        mBtnPrivacy             = (Button) root.findViewById(R.id.btn_privacy);
        mTvForgotPasswordLink   = (TextView) root.findViewById(R.id.tv_forgot_password_link);
        mTvAlreadyMemberLink    = (TextView) root.findViewById(R.id.tv_already_member);

        // Initialize
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                mEdtSignInEmail, mEdtSignInPassword, mTvForgotPasswordLink,
                mEdtRegisterDisplayName, mEdtRegisterEmail, mEdtRegisterPassword,
                (TextView) root.findViewById(R.id.tv_or),
                (TextView) root.findViewById(R.id.tv_register_agreement));
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Bold,
                mBtnSignIn, mBtnNewMember, mBtnRegister, mBtnEula, mBtnPrivacy);

        mFormValidator.addView("display_name" + VALIDATION_PREFIX_REGISTER, "Display Name", mEdtRegisterDisplayName);
        mFormValidator.addView("email" + VALIDATION_PREFIX_REGISTER, "E-mail", mEdtRegisterEmail);
        mFormValidator.addView("password" + VALIDATION_PREFIX_REGISTER, "Password", mEdtRegisterPassword);
        mFormValidator.addView("email" + VALIDATION_PREFIX_SIGNIN, "E-mail", mEdtSignInEmail);
        mFormValidator.addView("password" + VALIDATION_PREFIX_SIGNIN, "Password", mEdtSignInPassword);

        if(mCurrentState == null) {
            ILoginView.ViewState loginState =
                    (ILoginView.ViewState) getArguments().getSerializable(LoginActivity.BUNDLE_INITIATE_LOGIN_STATE);
            if (loginState == null) {
                mCurrentState = ViewState.Login;
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
        mTvAlreadyMemberLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just show another view
                changeState(ViewState.Login);
            }
        });
        mBtnNewMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just show another view;
                changeState(ViewState.Register);
            }
        });
        mTvForgotPasswordLink.setOnClickListener(new View.OnClickListener() {
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

                String display = mEdtRegisterDisplayName.getText().toString();
                String email = mEdtRegisterEmail.getText().toString();
                String password = mEdtRegisterPassword.getText().toString();
                EventBus.getInstance().post(new RegisterEvent(display, email, password));
            }
        });
        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
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
        mEdtRegisterPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
                    mBtnSignIn.performClick();
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
            mRloSignIn.setVisibility(View.VISIBLE);
            mRloRegister.setVisibility(View.GONE);
            mTvForgotPasswordLink.setVisibility(View.VISIBLE);
            mTvAlreadyMemberLink.setVisibility(View.GONE);
        } else {
            mRloSignIn.setVisibility(View.GONE);
            mRloRegister.setVisibility(View.VISIBLE);
            mTvForgotPasswordLink.setVisibility(View.GONE);
            mTvAlreadyMemberLink.setVisibility(View.VISIBLE);
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
