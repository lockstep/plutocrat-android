package com.whitefly.plutocrat.mainmenu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.text.CustomTypefaceSpan;
import com.whitefly.plutocrat.mainmenu.MainMenuActivity;
import com.whitefly.plutocrat.mainmenu.events.ExecuteShareEvent;
import com.whitefly.plutocrat.mainmenu.events.MoreShareClickEvent;
import com.whitefly.plutocrat.models.NewBuyoutModel;
import com.whitefly.plutocrat.models.TargetModel;

import java.lang.reflect.Field;

/**
 * Created by Satjapot on 5/19/16 AD.
 */
public class InitiateFragment extends DialogFragment {
    private static final String BUNDLE_TARGET = "targetModel";
    private static final String BUNDLE_NEW_BUYOUT = "buyoutModel";

    // Attributes
    private int mMin, mMax;
    private TargetModel mTarget;
    private View.OnClickListener mAbortClick;

    // Views
    private LinearLayout mLloPlutocrat, mLloTarget;
    private TextView mTvTargetName, mTvTargetBuyout, mTvTargetThreat, mTvTargetSurvived;
    private TextView mTvPlutocratName, mTvPlutocratBuyouts;
    private TextView mTvUseShares, mTvAvailible, mTvMinimum, mTvNote;
    private Button mBtnAbort, mBtnExecute, mBtnTargetAbort, mBtnPlutocratAbort;
    private ImageView mImvPlutocrat, mImvTarget;
    private TextView mTvPlutocratProfile, mTvTargetProfile;
    private TextView mTvLoadingMessage;

    private SeekBar mShareSeekBar;
    private NumberPicker mPicker;
    private AlertDialog mShareDialog, mBuyMoreDialog, mLoadingDialog;
    private CustomTypefaceSpan mFontSpan;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static InitiateFragment newInstance(TargetModel target, NewBuyoutModel newBuyout) {
        Gson gson = new Gson();
        InitiateFragment fragment = new InitiateFragment();
        fragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme);

        // Send target model to it
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TARGET, gson.toJson(target, TargetModel.class));
        bundle.putString(BUNDLE_NEW_BUYOUT, gson.toJson(newBuyout, NewBuyoutModel.class));
        fragment.setArguments(bundle);

        return fragment;
    }

    // Methods
    public AlertDialog getLoadingDialog(String message) {
        if(message == null) {
            mTvLoadingMessage.setText(getString(R.string.loading_default));
        } else {
            mTvLoadingMessage.setText(message);
        }
        return mLoadingDialog;
    }

    private void createShareDialog(LayoutInflater inflater, ViewGroup parent) {
        View dialog = inflater.inflate(R.layout.dialog_numberpicker, parent, false);
        mPicker = (NumberPicker) dialog.findViewById(R.id.numberPicker);

        SpannableString dialogButtonText = new SpannableString(getString(R.string.caption_moreshares));
        dialogButtonText.setSpan(
               mFontSpan, 0, dialogButtonText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mShareDialog = new AlertDialog.Builder(getActivity())
                .setView(dialog)
                .setNeutralButton(dialogButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventBus.getInstance().post(new MoreShareClickEvent());

                        dialog.dismiss();
                    }
                })
                .create();
    }

    private void createBuyMoreDialog() {
        SpannableString titleText = new SpannableString(getString(R.string.dialog_title_moreshare));
        titleText.setSpan( mFontSpan, 0, titleText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString contentText = new SpannableString(getString(R.string.dialog_content_moreshare));
        contentText.setSpan( mFontSpan, 0, contentText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString negativeText = new SpannableString(getString(R.string.caption_dismiss));
        negativeText.setSpan( mFontSpan, 0, negativeText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString positiveText = new SpannableString(getString(R.string.caption_moreshares));
        positiveText.setSpan( mFontSpan, 0, positiveText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);


        mBuyMoreDialog = new AlertDialog.Builder(getActivity())
                .setTitle(titleText)
                .setMessage(contentText)
                .setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainMenuActivity activity = (MainMenuActivity) getActivity();
                        activity.goToShareFromInitiate();
                    }
                })
                .setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    private void createLoadingDialog(LayoutInflater inflater, ViewGroup parent) {
        View root = inflater.inflate(R.layout.dialog_loading, parent, false);
        mTvLoadingMessage = (TextView) root.findViewById(R.id.tv_loading_message);

        mLoadingDialog = new AlertDialog.Builder(getActivity())
                .setView(root)
                .setCancelable(false)
                .create();
        mLoadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

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

        Gson gson = new Gson();
        mTarget = gson.fromJson(getArguments().getString(BUNDLE_TARGET), TargetModel.class);
        NewBuyoutModel newBuyout = gson.fromJson(getArguments().getString(BUNDLE_NEW_BUYOUT), NewBuyoutModel.class);
        mFontSpan = new CustomTypefaceSpan("", AppPreference.getInstance().getFont(AppPreference.FontType.Regular));

        createShareDialog(inflater, container);
        createLoadingDialog(inflater, container);
        createBuyMoreDialog();

        mMin = newBuyout.minimumAmount;
        mMax = newBuyout.availableShareCount;

        try {
            Field dividerField = mPicker.getClass().getDeclaredField("mSelectionDivider");

            dividerField.setAccessible(true);
            dividerField.set(mPicker, ContextCompat.getDrawable(getActivity(), R.drawable.numberpicker_line));
            mPicker.invalidate();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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

        mShareSeekBar.setMax(mMax - mMin);

        if(mMin > mMax || mMax == 0) {
            mTvAvailible.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorRed));
            mTvUseShares.setText(String.valueOf(mMax));
            mPicker.setMinValue(mMax);
            mPicker.setMaxValue(mMax);
        } else {
            mTvUseShares.setText(String.valueOf(mMin));
            mPicker.setMinValue(mMin);
            mPicker.setMaxValue(mMax);
        }

        // Initiate
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                mTvAvailible, mTvMinimum, mTvNote, mTvPlutocratProfile,
                mTvPlutocratName, mTvPlutocratBuyouts, mTvTargetProfile, mTvTargetName,
                mTvTargetThreat, mTvTargetBuyout, mTvTargetSurvived, mBtnExecute,
                mBtnAbort, mBtnPlutocratAbort, mBtnTargetAbort,
                (TextView) root.findViewById(R.id.tv_initiate_caption),
                (TextView) root.findViewById(R.id.tv_shares_caption));

        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Bold, mTvUseShares);

        if(mTarget.isPlutocrat) {
            mLloPlutocrat.setVisibility(View.VISIBLE);
            mLloTarget.setVisibility(View.GONE);

            Glide.with(getActivity()).load(mTarget.profileImage)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model,
                                                   Target<GlideDrawable> target, boolean isFirstResource) {
                            mImvPlutocrat.setVisibility(View.GONE);
                            mTvPlutocratProfile.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource,
                                                       String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            mImvPlutocrat.setVisibility(View.VISIBLE);
                            mTvPlutocratProfile.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mImvPlutocrat);
            mTvPlutocratName.setText(mTarget.name);
            mTvPlutocratBuyouts.setText(String.format(getActivity().getString(R.string.value_plutocrat_buyouts), mTarget.numSuccessfulBuyout));
        } else {
            mLloPlutocrat.setVisibility(View.GONE);
            mLloTarget.setVisibility(View.VISIBLE);

            Glide.with(getActivity()).load(mTarget.profileImage)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model,
                                                   Target<GlideDrawable> target, boolean isFirstResource) {
                            mImvPlutocrat.setVisibility(View.GONE);
                            mTvPlutocratProfile.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource,
                                                       String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            mImvPlutocrat.setVisibility(View.VISIBLE);
                            mTvPlutocratProfile.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mImvPlutocrat);
            mTvTargetName.setText(mTarget.name);
            mTvTargetBuyout.setText(String.format(getActivity().getString(R.string.value_buyouts), mTarget.numSuccessfulBuyout));
            mTvTargetThreat.setText(String.format(getActivity().getString(R.string.value_threats), mTarget.numMatchedBuyout));
            mTvTargetSurvived.setText(String.format(getActivity().getString(R.string.value_daysurvived), mTarget.getDaySurvived()));
        }

        mTvAvailible.setText(String.format(getActivity().getString(R.string.caption_available_shares), mMax));
        mTvMinimum.setText(String.format(getActivity().getString(R.string.caption_minimum_buyout), mMin));

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
                if(mMin > mMax || mMax == 0) {
                    mBuyMoreDialog.show();
                } else {
                    int useShare = Integer.valueOf(mTvUseShares.getText().toString());

                    EventBus.getInstance().post(new ExecuteShareEvent(mTarget.id, useShare));
                }
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
                mTvUseShares.setText(String.valueOf(mMin + progress));
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
