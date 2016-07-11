package com.whitefly.plutocrat.login.fragments;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
import com.whitefly.plutocrat.helpers.text.CustomTypefaceSpan;
import com.whitefly.plutocrat.login.events.BackToLoginEvent;
import com.whitefly.plutocrat.login.events.RequestResetTokenEvent;
import com.whitefly.plutocrat.login.events.ResetPassword1ErrorEvent;
import com.whitefly.plutocrat.login.views.ILoginView;
import com.whitefly.plutocrat.models.MetaModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResetPassword1Fragment extends Fragment {

    // Attributes
    private FormValidationHelper mFormValidator;

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

        mFormValidator = new FormValidationHelper();
        EventBus.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getInstance().unregister(this);
        mEdtEmail.setError(null);
    }

    public ResetPassword1Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_resetpw1_v2, container, false);
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
                mEdtEmail, mTvTitle, mTvContent, mTvResetCaption, mTvNote);
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Bold,
                mBtnLoginLink, mBtnRegisterLink, mBtnSubmit);

        mFormValidator.addView("email", "E-mail", mEdtEmail);
        mFormValidator.omitNameOfCustomMessage();

        Spannable note = SpannableString.valueOf(getString(R.string.have_token_content));
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String email = mEdtEmail.getText().toString();
                EventBus.getInstance().post(new RequestResetTokenEvent(email, false));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        note.setSpan(span, 0, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        note.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        note.setSpan(new CustomTypefaceSpan("", AppPreference.getInstance().getFont(AppPreference.FontType.Bold)),
                0, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mTvNote.setText(note);
        mTvNote.setMovementMethod(LinkMovementMethod.getInstance());

        // Event Handler
        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

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
        mEdtEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    mBtnSubmit.performClick();
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
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mEdtEmail != null) {
            mEdtEmail.setError(null);
        }
    }

    @Subscribe
    public void onResetPassword1Error(ResetPassword1ErrorEvent event) {
        MetaModel meta = event.getMetaModel();
        try {
            FormValidationHelper.ValidationRule rules = mFormValidator.begin();
            for(String key : meta.getKeys()) {
                rules.ruleRaiseError(key, meta.getValue(key));
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
