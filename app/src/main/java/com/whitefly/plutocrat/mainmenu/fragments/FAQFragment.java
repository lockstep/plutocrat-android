package com.whitefly.plutocrat.mainmenu.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;

/**
 * Created by satjapotiamopas on 5/24/16 AD.
 */
public class FAQFragment extends DialogFragment {

    // Attributes

    // Views
    private LinearLayout mLloBack;
    private TextView mTvNotePlutocrat;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FAQFragment.
     */
    public static FAQFragment newInstance() {
        FAQFragment fragment = new FAQFragment();
        fragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme);

        return fragment;
    }

    // Methods
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_faq, container, false);

        mLloBack = (LinearLayout) root.findViewById(R.id.btn_back);
        mTvNotePlutocrat = (TextView) root. findViewById(R.id.tv_faq_note_plutocrat) ;

        // Initialize
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                (TextView) root.findViewById(R.id.tv_faq_header_text),
                (TextView) root.findViewById(R.id.tv_btn_back),
                (TextView) root.findViewById(R.id.tv_faq_title_1),
                (TextView) root.findViewById(R.id.tv_faq_title_2),
                (TextView) root.findViewById(R.id.tv_faq_title_3),
                (TextView) root.findViewById(R.id.tv_faq_note_2),
                (TextView) root.findViewById(R.id.tv_faq_note_3),
                mTvNotePlutocrat);

        mTvNotePlutocrat.setText(Html.fromHtml(getActivity().getString(R.string.note_faq_plutocrat)));

        // Event handler
        mLloBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FAQFragment.this.dismiss();
            }
        });

        return root;
    }
}
