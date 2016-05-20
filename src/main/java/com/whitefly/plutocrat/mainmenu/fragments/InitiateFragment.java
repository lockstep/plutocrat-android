package com.whitefly.plutocrat.mainmenu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.mainmenu.events.MoreShareClickEvent;
import com.whitefly.plutocrat.models.TargetModel;

/**
 * Created by Satjapot on 5/19/16 AD.
 */
public class InitiateFragment extends DialogFragment {
    private static final String BUNDLE_TARGET = "targetModel";

    // Attributes
    private int mMin, mMax;
    private View.OnClickListener mAbortClick;

    // Views
    private LinearLayout mLloPlutocrat, mLloTarget;
    private TextView mTvTargetName, mTvTargetBuyout, mTvTargetThreat, mTvTargetSurvived;
    private TextView mTvPlutocratName, mTvPlutocratBuyouts;
    private TextView mTvUseShares, mTvAvailible, mTvMinimum, mTvNote;
    private Button mBtnAbort, mBtnExecute, mBtnTargetAbort, mBtnPlutocratAbort;
    private ImageView mImvPlutocrat, mImvTarget;
    private TextView mTvPlutocratProfile, mTvTargetProfile;

    private SeekBar mShareSeekBar;
    private NumberPicker mPicker;
    private AlertDialog mShareDialog;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static InitiateFragment newInstance(TargetModel model) {
        Gson gson = new Gson();
        InitiateFragment fragment = new InitiateFragment();
        fragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme);

        // Send target model to it
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TARGET, gson.toJson(model, TargetModel.class));
        fragment.setArguments(bundle);

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
        View root = inflater.inflate(R.layout.fragment_initiate, container, false);
        View dialog = inflater.inflate(R.layout.dialog_numberpicker, null, false);

        // Get views
        mLloPlutocrat       = (LinearLayout) root.findViewById(R.id.llo_initiate_plutocrat);
        mLloTarget          = (LinearLayout) root.findViewById(R.id.llo_initiate_target);
        mTvUseShares        = (TextView) root.findViewById(R.id.tv_initiate_share);
        mTvAvailible        = (TextView) root.findViewById(R.id.tv_available_shares);
        mTvMinimum          = (TextView) root.findViewById(R.id.tv_minimum_buyouts);
        mTvNote             = (TextView) root.findViewById(R.id.tv_initiate_note);
        mImvPlutocrat       = (ImageView) root.findViewById(R.id.imv_plutocrat_profile);
        mTvPlutocratProfile = (TextView) root.findViewById(R.id.tv_plutocrat_profile_nickname);
        mTvPlutocratName    = (TextView) root.findViewById(R.id.tv_plutocrat_name);
        mTvPlutocratBuyouts = (TextView) root.findViewById(R.id.tv_plutocrat_buyouts);
        mImvTarget          = (ImageView) root.findViewById(R.id.imv_profile);
        mTvTargetProfile    = (TextView) root.findViewById(R.id.tv_profile_nickname);
        mTvTargetName       = (TextView) root.findViewById(R.id.tv_player_name);
        mTvTargetThreat     = (TextView) root.findViewById(R.id.tv_player_threats);
        mTvTargetBuyout     = (TextView) root.findViewById(R.id.tv_player_buyouts);
        mTvTargetSurvived   = (TextView) root.findViewById(R.id.tv_player_daysurvived);
        mBtnExecute         = (Button) root.findViewById(R.id.btn_initiate_execute);
        mBtnAbort           = (Button) root.findViewById(R.id.btn_initiate_abort);
        mBtnPlutocratAbort  = (Button) root.findViewById(R.id.btn_plutocrat_abort);
        mBtnTargetAbort     = (Button) root.findViewById(R.id.btn_target_abort);
        mShareSeekBar       = (SeekBar) root.findViewById(R.id.seekBar);
        mPicker             = (NumberPicker) dialog.findViewById(R.id.numberPicker);

        mMin = 12;
        mMax = 23;
        mTvUseShares.setText(String.valueOf(mMin));

        Gson gson = new Gson();
        TargetModel target = gson.fromJson(getArguments().getString(BUNDLE_TARGET), TargetModel.class);

        mPicker.setMinValue(mMin);
        mPicker.setMaxValue(mMax);
        mPicker.setWrapSelectorWheel(false);
        mPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pickerValue = mPicker.getValue();
                mTvUseShares.setText(String.valueOf(pickerValue));
                mShareSeekBar.setProgress(pickerValue - mMin);

                mShareDialog.dismiss();
            }
        });
        mShareDialog = new AlertDialog.Builder(getActivity())
                .setView(dialog)
                .setNeutralButton(R.string.caption_moreshares, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventBus.getInstance().post(new MoreShareClickEvent());

                        dialog.dismiss();
                    }
                })
                .create();

        mShareSeekBar.setMax(mMax - mMin);

        // Initiate
        if(target.isPlutocrat) {
            mLloPlutocrat.setVisibility(View.VISIBLE);
            mLloTarget.setVisibility(View.GONE);

            if(target.picProfile == 0) {
                mImvPlutocrat.setVisibility(View.GONE);
                mTvPlutocratProfile.setVisibility(View.VISIBLE);

                mTvPlutocratProfile.setText(target.getNickName());
            } else {
                mImvPlutocrat.setVisibility(View.VISIBLE);
                mTvPlutocratProfile.setVisibility(View.GONE);

                mImvPlutocrat.setImageDrawable(ContextCompat.getDrawable(getActivity(), target.picProfile));
            }
            mTvPlutocratName.setText(target.name);
            mTvPlutocratBuyouts.setText(String.format(getActivity().getString(R.string.value_plutocrat_buyouts), target.numBuyouts));
        } else {
            mLloPlutocrat.setVisibility(View.GONE);
            mLloTarget.setVisibility(View.VISIBLE);

            if(target.picProfile == 0) {
                mImvTarget.setVisibility(View.GONE);
                mTvTargetProfile.setVisibility(View.VISIBLE);

                mTvTargetProfile.setText(target.getNickName());
            } else {
                mImvTarget.setVisibility(View.VISIBLE);
                mTvTargetProfile.setVisibility(View.GONE);

                mImvTarget.setImageDrawable(ContextCompat.getDrawable(getActivity(), target.picProfile));
            }
            mTvTargetName.setText(target.name);
            mTvTargetBuyout.setText(String.format(getActivity().getString(R.string.value_buyouts), target.numBuyouts));
            mTvTargetThreat.setText(String.format(getActivity().getString(R.string.value_threats), target.numThreats));
            mTvTargetSurvived.setText(String.format(getActivity().getString(R.string.value_daysurvived), target.daySurvived));
        }

        mTvAvailible.setText(String.format(getActivity().getString(R.string.caption_available_shares), 23));
        mTvMinimum.setText(String.format(getActivity().getString(R.string.caption_minimum_buyout), 12));

        mTvNote.setText(Html.fromHtml(getActivity().getString(R.string.initiate_content)));

        // Event Handler
        mAbortClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InitiateFragment.this.dismiss();
            }
        };
        mBtnAbort.setOnClickListener(mAbortClick);
        mBtnPlutocratAbort.setOnClickListener(mAbortClick);
        mBtnTargetAbort.setOnClickListener(mAbortClick);

        mBtnExecute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something

                InitiateFragment.this.dismiss();
            }
        });

        mTvUseShares.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShareDialog.show();
            }
        });

        mShareSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTvUseShares.setText(String.valueOf(12 + progress));
                mPicker.setValue(mMin + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return root;
    }
}
