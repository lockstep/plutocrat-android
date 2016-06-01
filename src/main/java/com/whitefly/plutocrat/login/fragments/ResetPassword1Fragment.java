package com.whitefly.plutocrat.login.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
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
import com.whitefly.plutocrat.login.events.RequestResetTokenEvent;
import com.whitefly.plutocrat.login.views.ILoginView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResetPassword1Fragment extends Fragment {

    // Attributes

    // Views
    private Button mBtnSubmit;
    private EditText mEdtEmail;
    private TextView mTvTitle, mTvContent, mTvResetCaption, mTvNote;
    private Button mBtnLoginLink, mBtnRegisterLink;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static ResetPassword1Fragment newInstance() {
        ResetPassword1Fragment fragment = new ResetPassword1Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ResetPassword1Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_resetpw1, container, false);
        mBtnSubmit      = (Button) root.findViewById(R.id.btn_reset1_submit);
        mEdtEmail       = (EditText) root.findViewById(R.id.edt_reset1_email);
        mTvTitle        = (TextView) root.findViewById(R.id.tv_reset1_title);
        mTvContent      = (TextView) root.findViewById(R.id.tv_reset1_content);
        mTvResetCaption = (TextView) root.findViewById(R.id.tv_reset1_caption);
        mTvNote         = (TextView) root.findViewById(R.id.tv_reset1_note);
        mBtnLoginLink   = (Button) root.findViewById(R.id.btn_reset1_login_link);
        mBtnRegisterLink = (Button) root.findViewById(R.id.btn_reset1_register_link);

        // Initialize
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                mBtnLoginLink, mEdtEmail, mTvTitle, mTvContent, mTvResetCaption, mTvNote,
                mBtnLoginLink, mBtnRegisterLink, mBtnSubmit);

        Spannable note = SpannableString.valueOf(getString(R.string.havetoken_content));
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                EventBus.getInstance().post(new RequestResetTokenEvent("", false));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        note.setSpan(span, 0, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        note.setSpan(new StyleSpan(R.style.LinkText), 0, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mTvNote.setText(note);
        mTvNote.setMovementMethod(LinkMovementMethod.getInstance());

        // Event Handler
        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEdtEmail.getText().toString();
                EventBus.getInstance().post(new RequestResetTokenEvent(email, true));
            }
        });
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
