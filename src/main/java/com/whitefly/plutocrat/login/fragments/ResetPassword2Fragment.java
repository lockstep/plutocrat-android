package com.whitefly.plutocrat.login.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.login.events.BackToLoginEvent;
import com.whitefly.plutocrat.login.views.ILoginView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResetPassword2Fragment extends Fragment {

    // Attributes
    private TextView mTvLoginLink, mTvRegisterLink;

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
        mTvLoginLink = (TextView) root.findViewById(R.id.tv_reset2_login_link);
        mTvRegisterLink = (TextView) root.findViewById(R.id.tv_reset2_register_link);

        // Event Handler
        mTvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getInstance().post(new BackToLoginEvent(ILoginView.ViewState.Login));
            }
        });
        mTvRegisterLink.setOnClickListener(new View.OnClickListener() {
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
