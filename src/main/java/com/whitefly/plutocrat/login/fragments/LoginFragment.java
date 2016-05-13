package com.whitefly.plutocrat.login.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.login.events.ForgotPasswordEvent;
import com.whitefly.plutocrat.login.events.RegisterEvent;
import com.whitefly.plutocrat.login.events.SignInEvent;
import com.whitefly.plutocrat.login.views.ILoginView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment implements ILoginView {
    // Attributes
    private ILoginView.ViewState mCurrentState;

    // Views
    private TextView mTvWelcomeContent;
    private TextView mTvLoginLink, mTvRegisterLink, mTvForgotPwLink;
    private RelativeLayout mRloRegister, mRloSignin;
    private Button mBtnRegister, mBtnSigin;
    private EditText mEdtRegDisplayName, mEdtRegEmail, mEdtRegPassword;
    private EditText mEdtSignInEmail, mEdtSignInPassword;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentState = ViewState.Register;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        // Get views
        mTvWelcomeContent   = (TextView) root.findViewById(R.id.tv_welcome_content);
        mTvLoginLink        = (TextView) root.findViewById(R.id.tv_login_link);
        mTvRegisterLink     = (TextView) root.findViewById(R.id.tv_register_link);
        mTvForgotPwLink     = (TextView) root.findViewById(R.id.tv_forgotpw_link);
        mRloRegister        = (RelativeLayout) root.findViewById(R.id.rlo_register);
        mRloSignin          = (RelativeLayout) root.findViewById(R.id.rlo_signin);
        mBtnRegister        = (Button) root.findViewById(R.id.btn_register);
        mBtnSigin           = (Button) root.findViewById(R.id.btn_signin);
        mEdtRegDisplayName  = (EditText) root.findViewById(R.id.edt_reg_displayname);
        mEdtRegEmail        = (EditText) root.findViewById(R.id.edt_reg_email);
        mEdtRegPassword     = (EditText) root.findViewById(R.id.edt_reg_pw);
        mEdtSignInEmail     = (EditText) root.findViewById(R.id.edt_signin_email);
        mEdtSignInPassword  = (EditText) root.findViewById(R.id.edt_signin_pw);

        // Initialize
        mTvWelcomeContent.setText(Html.fromHtml(getString(R.string.welcome_content)));

        // Event Handler
        mTvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just show another view
                changeState(ViewState.Login);
            }
        });
        mTvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just show another view;
                changeState(ViewState.Register);
            }
        });
        mTvForgotPwLink.setOnClickListener(new View.OnClickListener() {
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
                String email = mEdtSignInEmail.getText().toString();
                String password = mEdtSignInPassword.getText().toString();
                EventBus.getInstance().post(new SignInEvent(email, password));
            }
        });

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

    /*
    Implement ILoginView
     */
    @Override
    public void changeState(ViewState state) {
        if(state == ViewState.Login) {
            mRloRegister.setVisibility(View.GONE);
            mRloSignin.setVisibility(View.VISIBLE);
        } else {
            mRloRegister.setVisibility(View.VISIBLE);
            mRloSignin.setVisibility(View.GONE);
        }
        mCurrentState = state;
    }

    @Override
    public void toast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }
}
