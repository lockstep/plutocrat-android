package com.whitefly.plutocrat.mainmenu.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
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

    // Views
    private LinearLayout mLloThreat, mLloOwner, mLloNote, mLloHeader;
    private RelativeLayout mLloShares;
    private TextView mTvHeader, mTvTime;
    private TextView mTvOwnerName, mTvOwnerEmail, mTvOwnerPosition;
    private TextView mTvThreatName, mTvThreatMatch, mTvThreatNote;
    private TextView mTvSuccessValue, mTvFailedValue, mTvDefeatValue;
    private TextView mTvNote;
    private ImageView mImvOwnerPic, mImvThreatPic;

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
                mTvNote.setText(Html.fromHtml(String.format(mNoteDefault, 4)));
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
        mTvOwnerPosition    = (TextView) root.findViewById(R.id.tv_home_owner_position);
        mTvThreatName       = (TextView) root.findViewById(R.id.tv_home_threat_name);
        mTvThreatMatch      = (TextView) root.findViewById(R.id.tv_home_threat_match);
        mTvThreatNote       = (TextView) root.findViewById(R.id.tv_home_threat_note);
        mTvNote             = (TextView) root.findViewById(R.id.tv_home_note);
        mTvSuccessValue     = (TextView) root.findViewById(R.id.tv_home_success_value);
        mTvFailedValue     = (TextView) root.findViewById(R.id.tv_home_failed_value);
        mTvDefeatValue     = (TextView) root.findViewById(R.id.tv_home_defeat_value);
        mImvOwnerPic        = (ImageView) root.findViewById(R.id.imv_home_owner_pic);
        mImvThreatPic       = (ImageView) root.findViewById(R.id.imv_home_threat_pic);

        // Initiate
        mTvThreatNote.setText(Html.fromHtml(getString(R.string.home_threat_content)));

        // Force to render correctly
//        changeState(State.Threat);
        changeState(State.Default);

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
        return R.drawable.icon_menu_default;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
}
