package com.whitefly.plutocrat.mainmenu.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.mainmenu.MainMenuActivity;
import com.whitefly.plutocrat.mainmenu.views.ITabView;

/**
 * Created by Satjapot on 5/10/16 AD.
 */
public class HomeFragment extends Fragment implements ITabView {
    public static final String TITLE = "Home";

    public enum State {
        Default, Threat, Suspend
    }

    // Attributes
    private State mState;
    private String mHeaderDefault, mHeaderThreat, mHeaderSuspend;
    private String mNoteSuspend, mNoteDefault;
    private Drawable mDefaultBG, mThreatBG, mTopLineBG, mBottomLineBG;
    private ClickableSpan mClickToPermission, mClickToBuyouts;
    private StyleSpan mStyleLink;

    // Views
    private LinearLayout mLloThreat, mLloOwner, mLloNote, mLloHeader;
    private RelativeLayout mLloShares;
    private TextView mTvHeader, mTvTime;
    private TextView mTvOwnerName, mTvOwnerEmail;
    private TextView mTvThreatName, mTvThreatMatch, mTvThreatNote;
    private TextView mTvSuccessValue, mTvFailedValue, mTvDefeatValue;
    private TextView mTvNote;
    private ImageView mImvOwnerPic, mImvThreatPic;
    private Button mBtnOwnerPosition, mBtnMatchShares, mBtnAcceptDefeat;

    // Methods
    private void changeState(State state) {
        mState = state;
        switch (mState) {
            case Default:
                // Change header
                mTvHeader.setText(mHeaderDefault);
                mTvTime.setText("3d 14h 11s");
                mLloHeader.setBackground(mDefaultBG);

                // Change layout
                mLloThreat.setVisibility(View.GONE);
                mLloOwner.setVisibility(View.VISIBLE);
                mLloShares.setVisibility(View.VISIBLE);
                mLloNote.setVisibility(View.VISIBLE);

                // Set value
                Spanned spannedText = Html.fromHtml(String.format(mNoteDefault, 4));
                Spannable spanningText = SpannableString.valueOf(spannedText);
                ClickableSpan[] clickSpans = spanningText.getSpans(0, spannedText.length(), ClickableSpan.class);

                // Define new span tags
                int startTag;
                int endTag;
                startTag = spannedText.getSpanStart(clickSpans[0]);
                endTag = spannedText.getSpanEnd(clickSpans[0]);
                spanningText.removeSpan(clickSpans[0]);
                spanningText.setSpan(mClickToPermission, startTag, endTag, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spanningText.setSpan(mStyleLink, startTag, endTag, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                startTag = spannedText.getSpanStart(clickSpans[1]);
                endTag = spannedText.getSpanEnd(clickSpans[1]);
                spanningText.removeSpan(clickSpans[1]);
                spanningText.setSpan(mClickToBuyouts, startTag, endTag, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spanningText.setSpan(mStyleLink, startTag, endTag, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                mTvNote.setText(spanningText);
                mTvNote.setMovementMethod(LinkMovementMethod.getInstance());

                ((MainMenuActivity) getActivity()).activateMenu();
                break;
            case Threat:
                // Change header
                mTvHeader.setText(mHeaderThreat);
                mTvTime.setText("Deadline: 3d 14h 11s");
                mLloHeader.setBackground(mThreatBG);

                // Change layout
                mLloThreat.setVisibility(View.VISIBLE);
                mLloOwner.setVisibility(View.VISIBLE);
                mLloShares.setVisibility(View.GONE);
                mLloNote.setVisibility(View.GONE);

                mLloThreat.setBackground(mBottomLineBG);
                mLloOwner.setBackground(null);
                mLloShares.setBackground(null);
                mLloNote.setBackground(null);

                ((MainMenuActivity) getActivity()).activateMenu();
                break;
            case Suspend:
                // Change header
                mTvHeader.setText(mHeaderSuspend);
                mTvTime.setText("3d 14h 11s");
                mLloHeader.setBackground(mThreatBG);

                // Change layout
                mLloThreat.setVisibility(View.GONE);
                mLloOwner.setVisibility(View.VISIBLE);
                mLloShares.setVisibility(View.VISIBLE);
                mLloNote.setVisibility(View.VISIBLE);

                mLloThreat.setBackground(null);
                mLloOwner.setBackground(mBottomLineBG);
                mLloShares.setBackground(mBottomLineBG);
                mLloNote.setBackground(null);

                // Set value
                mTvNote.setText(Html.fromHtml(String.format(mNoteSuspend, "amy", 32, 4)));

                ((MainMenuActivity) getActivity()).suspendMenu();
                break;
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get resources
        mHeaderDefault  = getString(R.string.caption_you_survived);
        mHeaderThreat   = getString(R.string.caption_active_threat);
        mHeaderSuspend  = getString(R.string.caption_you_survived);
        mNoteDefault    = getString(R.string.home_default_content);
        mNoteSuspend    = getString(R.string.home_suspend_content);
        mDefaultBG      = ContextCompat.getDrawable(getActivity(), R.drawable.header_bg_default);
        mThreatBG       = ContextCompat.getDrawable(getActivity(), R.drawable.header_bg_under_threat);
        mTopLineBG      = ContextCompat.getDrawable(getActivity(), R.drawable.bg_line_top);
        mBottomLineBG   = ContextCompat.getDrawable(getActivity(), R.drawable.bg_line_bottom);

        // Initiate
        mClickToPermission = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Toast.makeText(getActivity(), "Tab here click", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        mClickToBuyouts = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Toast.makeText(getActivity(), "Another click", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        mStyleLink = new StyleSpan(R.style.LinkText);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mLloThreat          = (LinearLayout) root.findViewById(R.id.llo_home_threat);
        mLloOwner           = (LinearLayout) root.findViewById(R.id.llo_home_owner);
        mLloShares          = (RelativeLayout) root.findViewById(R.id.llo_home_shares);
        mLloNote            = (LinearLayout) root.findViewById(R.id.llo_home_note);
        mLloHeader          = (LinearLayout) root.findViewById(R.id.llo_home_header);
        mTvHeader           = (TextView) root.findViewById(R.id.tv_home_title);
        mTvTime             = (TextView) root.findViewById(R.id.tv_home_time);
        mTvOwnerName        = (TextView) root.findViewById(R.id.tv_home_owner_name);
        mTvOwnerEmail       = (TextView) root.findViewById(R.id.tv_home_owner_email);
        mTvThreatName       = (TextView) root.findViewById(R.id.tv_home_threat_name);
        mTvThreatMatch      = (TextView) root.findViewById(R.id.tv_home_threat_match);
        mTvThreatNote       = (TextView) root.findViewById(R.id.tv_home_threat_note);
        mTvNote             = (TextView) root.findViewById(R.id.tv_home_note);
        mTvSuccessValue     = (TextView) root.findViewById(R.id.tv_home_success_value);
        mTvFailedValue     = (TextView) root.findViewById(R.id.tv_home_failed_value);
        mTvDefeatValue     = (TextView) root.findViewById(R.id.tv_home_defeat_value);
        mImvOwnerPic        = (ImageView) root.findViewById(R.id.imv_home_owner_pic);
        mImvThreatPic       = (ImageView) root.findViewById(R.id.imv_home_threat_pic);
        mBtnOwnerPosition   = (Button) root.findViewById(R.id.btn_home_owner_position);
        mBtnMatchShares     = (Button) root.findViewById(R.id.btn_match_shares);
        mBtnAcceptDefeat    = (Button) root.findViewById(R.id.btn_accept_defeat);

        // Initiate
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                mTvHeader, mTvTime, mTvOwnerName, mTvOwnerEmail, mBtnOwnerPosition,
                mTvThreatName, mTvThreatMatch, mTvThreatNote, mTvNote,
                mBtnOwnerPosition, mBtnMatchShares, mBtnAcceptDefeat,
                (TextView) root.findViewById(R.id.tv_home_success),
                (TextView) root.findViewById(R.id.tv_home_failed),
                (TextView) root.findViewById(R.id.tv_home_defeat));
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Bold,
                mTvSuccessValue, mTvFailedValue, mTvDefeatValue);

        mTvThreatNote.setText(Html.fromHtml(getString(R.string.home_threat_content)));

        // Force to render correctly
        changeState(State.Default);

        // Event Handler
        mBtnOwnerPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainMenuActivity) HomeFragment.this.getActivity()).showAccountSettingFragment();
            }
        });

        // Debug
        mLloHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mState == State.Default) {
                    changeState(State.Threat);
                } else if(mState == State.Threat) {
                    changeState(State.Suspend);
                } else {
                    changeState(State.Default);
                }
            }
        });

        return root;
    }

    @Override
    public int getIcon() {
        return R.drawable.icon_menu_home;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
}
