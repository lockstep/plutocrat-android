package com.whitefly.plutocrat.login.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;

/**
 * Created by Satjapot on 6/1/16 AD.
 */
public class WebViewFragment extends DialogFragment implements View.OnClickListener {
    private static final int MAX_PROGRESS = 100;
    private static final int TIME_HIDE_PROGRESS = 300;

    private TextView mTvTitle;
    private WebView mWvMain;
    private LinearLayout mLloBack;
    private ProgressBar mPgbLoading;

    private CharSequence mTitle;
    private String mUrl;

    public WebViewFragment() {
        // Required empty public constructor
    }

    public static WebViewFragment newInstance() {
        WebViewFragment fragment = new WebViewFragment();
        fragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme);
        return fragment;
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
    }

    public void loadUrl(String url) {
        mUrl = url;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_webview, container, false);
        mTvTitle = (TextView) root.findViewById(R.id.tv_webview_header);
        mWvMain = (WebView) root.findViewById(R.id.web_view);
        mLloBack = (LinearLayout) root.findViewById(R.id.btn_back);
        mPgbLoading = (ProgressBar) root.findViewById(R.id.pgb_loading);

        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                mTvTitle, (TextView) root.findViewById(R.id.tv_btn_back));

        mWvMain.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(mUrl);

                return true;
            }
        });

        mWvMain.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                mPgbLoading.setProgress(newProgress);
                if(newProgress == MAX_PROGRESS) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPgbLoading.setVisibility(ProgressBar.GONE);
                        }
                    }, TIME_HIDE_PROGRESS);
                }
            }
        });

        mTvTitle.setText(mTitle);
        mWvMain.loadUrl(mUrl);

        mLloBack.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View v) {
        if(v == mLloBack) {
            dismiss();
        }
    }
}
