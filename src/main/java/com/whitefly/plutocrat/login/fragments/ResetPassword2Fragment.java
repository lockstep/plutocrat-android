package com.whitefly.plutocrat.login.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.login.events.BackToLoginEvent;
import com.whitefly.plutocrat.login.views.ILoginView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResetPassword2Fragment extends Fragment {

    // Attributes
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
    }

    public ResetPassword2Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get Views
        View root = inflater.inflate(R.layout.fragment_resetpw2, container, false);
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
                mTvTitle, mTvContent, mTvResetCaption, mBtnLoginLink, mBtnRegisterLink,
                mEdtEmail, mEdtResetToken, mEdtNewPassword, mEdtConfirmPassword, mBtnReset);

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

        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
