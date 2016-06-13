package com.whitefly.plutocrat.mainmenu.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.mainmenu.views.ITabView;

/**
 * Created by Satjapot on 5/10/16 AD.
 */
public class AboutFragment extends Fragment implements ITabView {
    public static final String TITLE = "About";
    private static final int MAX_PROGRESS = 100;
    private static final int TIME_HIDE_PROGRESS = 300;

    // Attributes

    // Views
    private WebView mWebView;
    private ProgressBar mPgbLoading;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about, container, false);
        mWebView = (WebView) root.findViewById(R.id.wv_about);
        mPgbLoading = (ProgressBar) root.findViewById(R.id.pgb_loading);

        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Light,
                (TextView) root.findViewById(R.id.tv_title_about));

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mPgbLoading.setVisibility(ProgressBar.GONE);
            }
        });

        mWebView.loadUrl(getString(R.string.about_url));

        return root;
    }

    @Override
    public int getIcon() {
        return R.drawable.icon_menu_about;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void updateView() {

    }
}
