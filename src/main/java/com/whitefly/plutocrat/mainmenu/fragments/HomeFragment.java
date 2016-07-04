package com.whitefly.plutocrat.mainmenu.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.otto.Subscribe;
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.text.CustomTypefaceSpan;
import com.whitefly.plutocrat.mainmenu.MainMenuActivity;
import com.whitefly.plutocrat.mainmenu.events.AttackTimeOutEvent;
import com.whitefly.plutocrat.mainmenu.events.CheckNotificationEnableEvent;
import com.whitefly.plutocrat.mainmenu.events.EnablePushNotificationEvent;
import com.whitefly.plutocrat.mainmenu.events.FailMatchBuyoutEvent;
import com.whitefly.plutocrat.mainmenu.events.MatchBuyoutEvent;
import com.whitefly.plutocrat.mainmenu.events.SetHomeStateEvent;
import com.whitefly.plutocrat.mainmenu.events.SignOutEvent;
import com.whitefly.plutocrat.mainmenu.events.UpdateUserNoticeIdEvent;
import com.whitefly.plutocrat.mainmenu.views.IHomeView;
import com.whitefly.plutocrat.mainmenu.views.ITabView;
import com.whitefly.plutocrat.mainmenu.views.events.MatchBuyoutCompletedEvent;
import com.whitefly.plutocrat.mainmenu.views.events.UpdateHomeViewEvent;
import com.whitefly.plutocrat.models.TargetModel;
import com.whitefly.plutocrat.models.UserModel;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by Satjapot on 5/10/16 AD.
 */
public class HomeFragment extends Fragment implements ITabView, IHomeView {
    public static final String TITLE = "Home";
    public static final long NO_DELAY = 0L;
    public static final long ONE_SECOND = 1000L;

    private static final int NUMBER_OF_DAY_TO_DEADLINE = 2;
    private static final String NO_TIME_PREFIX = "";

    public enum State {
        Default, Threat, Suspend
    }

    // Attributes
    private State mState;
    private String mHeaderDefault, mHeaderThreat, mHeaderSuspend, mPrefixTime;
    private String mNoteSuspend, mNoteDefault;
    private Drawable mDefaultBG, mThreatBG;
    private ClickableSpan mClickToBuyouts;
    private StyleSpan mStyleLink;
    private AlertDialog mNotificationEnableDialog, mSuspendingErrorDialog;

    private Timer mTimer;
    private boolean mIsTimerRunning;
    private Date mIssueDate;

    // Views
    private LinearLayout mLloThreat, mLloOwner, mLloNote, mLloHeader;
    private LinearLayout mLloNoteWrapper, mLloActiveDefaultNote, mLloFindTargetDefaultNote, mLloEnableNotificationNote;
    private RelativeLayout mLloShares;
    private TextView mTvHeader, mTvTime;
    private TextView mTvOwnerNickName, mTvOwnerName, mTvOwnerEmail;
    private TextView mTvThreatNickName, mTvThreatName, mTvThreatMatch, mTvThreatNote;
    private TextView mTvSuccessCaption, mTvFailedCaption, mTvDefeatCaption;
    private TextView mTvSuccessValue, mTvFailedValue, mTvDefeatValue;
    private TextView mTvNote;
    private ImageView mImvOwnerPic, mImvThreatPic;
    private Button mBtnOwnerPosition, mBtnMatchShares, mBtnAcceptDefeat;
    private Button mBtnFindTarget, mBtnEnableNotification;

    // Methods
    public void setupNotificationEnableDialog() {
        Typeface typeface = AppPreference.getInstance().getFont(AppPreference.FontType.Regular);
        TypefaceSpan span = new CustomTypefaceSpan("", typeface);

        Spannable title = SpannableString.valueOf(getString(R.string.caption_enable_push_notice));
        title.setSpan(span, 0, title.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        Spannable message = SpannableString.valueOf(getString(R.string.dialog_note_enable_push_notice));
        message.setSpan(span, 0, message.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        Spannable positiveText = SpannableString.valueOf(getString(R.string.caption_enable));
        positiveText.setSpan(span, 0, positiveText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        Spannable negativeText = SpannableString.valueOf(getString(R.string.caption_dismiss));
        negativeText.setSpan(span, 0, negativeText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        mNotificationEnableDialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventBus.getInstance().post(new EnablePushNotificationEvent());
                        EventBus.getInstance().post(new UpdateUserNoticeIdEvent(UserModel.NOTICE_DEFAULT));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventBus.getInstance().post(new UpdateUserNoticeIdEvent(UserModel.NOTICE_DEFAULT));
                        dialog.dismiss();
                    }
                })
                .create();
    }

    public void showSuspendErrorDialog() {
        Typeface typeface = AppPreference.getInstance().getFont(AppPreference.FontType.Regular);

        SpannableString spanTitle = new SpannableString(getString(R.string.error_title_suspending));
        spanTitle.setSpan(typeface, 0, spanTitle.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString spanMessage = new SpannableString(getString(R.string.error_suspending));
        spanMessage.setSpan(typeface, 0, spanMessage.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString negativeText = new SpannableString(getString(R.string.menu_sign_out));
        negativeText.setSpan(typeface, 0, negativeText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        if(mSuspendingErrorDialog != null) {
            mSuspendingErrorDialog.dismiss();
        }

        mSuspendingErrorDialog = new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle(spanTitle)
                .setMessage(spanMessage)
                .setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        EventBus.getInstance().post(new SignOutEvent());
                    }
                })
                .create();

        mSuspendingErrorDialog.show();
    }

    @Override
    public void changeState(State state, int noticeId) {
        mState = state;
        UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();

        switch (mState) {
            case Default:
                // Change header
                mTvHeader.setText(mHeaderDefault);
                mLloHeader.setBackground(mDefaultBG);

                // Change layout
                mLloThreat.setVisibility(View.GONE);
                mLloOwner.setVisibility(View.VISIBLE);
                mLloShares.setVisibility(View.VISIBLE);
                mLloNoteWrapper.setVisibility(View.VISIBLE);

                mLloThreat.setBackground(null);
                mLloOwner.setBackgroundResource(R.drawable.bg_line_bottom);
                mLloShares.setBackgroundResource(R.drawable.bg_line_bottom);
                mLloNote.setBackground(null);

                switch(noticeId) {
                    case UserModel.NOTICE_GETTING_STARTED:
                        mLloFindTargetDefaultNote.setVisibility(View.VISIBLE);
                        mLloEnableNotificationNote.setVisibility(View.GONE);
                        mLloNote.setVisibility(View.GONE);
                        break;
                    case UserModel.NOTICE_ENABLE_PUSH_NOTIFICATION:
                        mLloFindTargetDefaultNote.setVisibility(View.GONE);
                        mLloEnableNotificationNote.setVisibility(View.VISIBLE);
                        mLloNote.setVisibility(View.GONE);
                        break;
                    default:
                        mLloFindTargetDefaultNote.setVisibility(View.GONE);
                        mLloEnableNotificationNote.setVisibility(View.GONE);
                        mLloNote.setVisibility(View.VISIBLE);

                        // Set value
                        Spanned spannedText = Html.fromHtml(String.format(mNoteDefault, activeUser.numBuyoutUntilPlutocrat));
                        Spannable spanningText = SpannableString.valueOf(spannedText);
                        ClickableSpan[] clickSpans = spanningText.getSpans(0, spannedText.length(), ClickableSpan.class);

                        // Define new span tags
                        int startTag;
                        int endTag;

                        startTag = spannedText.getSpanStart(clickSpans[0]);
                        endTag = spannedText.getSpanEnd(clickSpans[0]);
                        spanningText.removeSpan(clickSpans[0]);
                        spanningText.setSpan(mClickToBuyouts, startTag, endTag, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        spanningText.setSpan(mStyleLink, startTag, endTag, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                        mTvNote.setText(spanningText);
                        mTvNote.setMovementMethod(LinkMovementMethod.getInstance());
                }

                ((MainMenuActivity) getActivity()).activateMenu();

                setOwnerView();

                startTimer();
                break;
            case Threat:
                // Change header
                mTvHeader.setText(mHeaderThreat);
                mLloHeader.setBackground(mThreatBG);

                // Change layout
                mLloThreat.setVisibility(View.VISIBLE);
                mLloOwner.setVisibility(View.VISIBLE);
                mLloShares.setVisibility(View.GONE);
                mLloNoteWrapper.setVisibility(View.GONE);

                mLloThreat.setBackgroundResource(R.drawable.bg_line_bottom);
                mLloOwner.setBackground(null);
                mLloShares.setBackground(null);
                mLloNote.setBackground(null);

                ((MainMenuActivity) getActivity()).activateMenu();

                setOwnerView();
                setInitiatingUserView();

                startTimer();
                break;
            case Suspend:
                // Change header
                mTvHeader.setText(mHeaderSuspend);
                mLloHeader.setBackground(mThreatBG);

                // Change layout
                mLloThreat.setVisibility(View.GONE);
                mLloOwner.setVisibility(View.VISIBLE);
                mLloShares.setVisibility(View.VISIBLE);
                mLloNoteWrapper.setVisibility(View.VISIBLE);

                mLloNote.setVisibility(View.VISIBLE);
                mLloFindTargetDefaultNote.setVisibility(View.GONE);
                mLloEnableNotificationNote.setVisibility(View.GONE);

                mLloThreat.setBackground(null);
                mLloOwner.setBackgroundResource(R.drawable.bg_line_bottom);
                mLloShares.setBackgroundResource(R.drawable.bg_line_bottom);
                mLloNote.setBackground(null);

                ((MainMenuActivity) getActivity()).suspendMenu();

                setOwnerView();
                setSuspendView();

                stopTimer();
                break;
        }
    }

    private void setOwnerView() {
        UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
        mTvOwnerNickName.setText(activeUser.getNickName());
        mTvOwnerName.setText(activeUser.name);
        mTvOwnerEmail.setText(activeUser.email);
        mTvSuccessValue.setText(String.valueOf(activeUser.numSuccessfulBuyout));
        mTvFailedValue.setText(String.valueOf(activeUser.numFailedBuyouts));
        mTvDefeatValue.setText(String.valueOf(activeUser.numMatchedBuyout));
        Glide.with(getActivity()).load(activeUser.profileImage)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        mTvOwnerNickName.setVisibility(View.VISIBLE);
                        mImvOwnerPic.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache, boolean isFirstResource) {
                        mTvOwnerNickName.setVisibility(View.GONE);
                        mImvOwnerPic.setVisibility(View.VISIBLE);

                        return false;
                    }
                })
                .bitmapTransform(new CropCircleTransformation(getActivity()))
                .into(mImvOwnerPic);
        if(activeUser.activeInboundBuyout != null) {
            if(activeUser.activeInboundBuyout.deadlineAt == null) {
                Calendar c = Calendar.getInstance();
                c.setTime(activeUser.activeInboundBuyout.initiatedAt);
                c.add(Calendar.DATE, NUMBER_OF_DAY_TO_DEADLINE);
                mIssueDate = c.getTime();
            } else {
                mIssueDate = activeUser.activeInboundBuyout.deadlineAt;
            }
            mPrefixTime = getString(R.string.caption_deadline_time) + " ";
        } else {
            mIssueDate = activeUser.registeredAt;
            mPrefixTime = NO_TIME_PREFIX;
        }
    }

    private void setInitiatingUserView() {
        TargetModel initiatingUser = AppPreference.getInstance().getSession().getInitiatingUser();
        UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
        String matchShare  = String.format(getString(R.string.caption_share_to_match),
                activeUser.activeInboundBuyout.numShares);

        mTvThreatNickName.setText(initiatingUser.getNickName());
        mTvThreatName.setText(initiatingUser.name);
        mTvThreatMatch.setText(matchShare);
        Glide.with(getActivity()).load(initiatingUser.profileImage)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        mTvThreatNickName.setVisibility(View.VISIBLE);
                        mImvThreatPic.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache, boolean isFirstResource) {
                        mTvThreatNickName.setVisibility(View.GONE);
                        mImvThreatPic.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .bitmapTransform(new CropCircleTransformation(getActivity()))
                .into(mImvThreatPic);
    }

    private void setSuspendView() {
        TargetModel terminalUser = AppPreference.getInstance().getSession().getTerminalUser();
        UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
        String numShare = String.format("%d %s", activeUser.terminalBuyout.numShares,
                activeUser.terminalBuyout.numShares == 1 ? "share" : "shares");
        long survivedTime = activeUser.defeatedAt.getTime() - activeUser.registeredAt.getTime();

        mTvNote.setText(Html.fromHtml(String.format(mNoteSuspend, terminalUser.name,
                numShare, activeUser.terminalBuyout.getTimeAgo())));
        mTvTime.setText(getTimeDurationString(survivedTime));
    }

    private String getTimeDurationString(long elapseTime) {
        long day = TimeUnit.MILLISECONDS.toDays(elapseTime);
        long hr = TimeUnit.MILLISECONDS.toHours(elapseTime - TimeUnit.DAYS.toMillis(day));
        long min = TimeUnit.MILLISECONDS.toMinutes(elapseTime - TimeUnit.DAYS.toMillis(day) - TimeUnit.HOURS.toMillis(hr));
        long sec = TimeUnit.MILLISECONDS.toSeconds(elapseTime - TimeUnit.DAYS.toMillis(day) - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));

        if(day == 0) {
            return String.format("%s%dh %dm %ds", mPrefixTime, hr, min, sec);
        } else {
            return String.format("%s%dd %dh %dm %ds", mPrefixTime, day, hr, min, sec);
        }
    }

    private void startTimer() {
        if(mIssueDate != null && mTimer == null) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Date currentTime = Calendar.getInstance().getTime();
                                long elapseTime = mIssueDate.getTime() - currentTime.getTime();
                                long elapseABSTime = Math.abs(elapseTime);

                                mTvTime.setText(getTimeDurationString(elapseABSTime));

                                if (mState == State.Threat && elapseTime < 0L) {
                                    stopTimer();
                                    EventBus.getInstance().post(new AttackTimeOutEvent());
                                }
                            }
                        });
                    }
                }
            }, NO_DELAY, ONE_SECOND);
        }
    }

    private void stopTimer() {
        stopTimer(false);
    }

    private void stopTimer(boolean fromPause) {
        if(mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            if(! fromPause) {
                mIsTimerRunning = false;
            }
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters
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
        mHeaderDefault  = getString(R.string.caption_survival_time);
        mHeaderThreat   = getString(R.string.caption_active_threat);
        mHeaderSuspend  = getString(R.string.caption_you_survived);
        mNoteDefault    = getString(R.string.home_default_content);
        mNoteSuspend    = getString(R.string.home_suspend_content);
        mDefaultBG      = ContextCompat.getDrawable(getActivity(), R.drawable.header_bg_default);
        mThreatBG       = ContextCompat.getDrawable(getActivity(), R.drawable.header_bg_under_threat);

        setupNotificationEnableDialog();

        // Initiate
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
        mIsTimerRunning = false;

        EventBus.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
        EventBus.getInstance().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mLloThreat          = (LinearLayout) root.findViewById(R.id.llo_home_threat);
        mLloOwner           = (LinearLayout) root.findViewById(R.id.llo_home_owner);
        mLloShares          = (RelativeLayout) root.findViewById(R.id.llo_home_shares);
        mLloHeader          = (LinearLayout) root.findViewById(R.id.llo_home_header);
        mLloNoteWrapper     = (LinearLayout) root.findViewById(R.id.llo_home_note_wrapper);
        mLloNote            = (LinearLayout) root.findViewById(R.id.llo_home_note);
        mLloFindTargetDefaultNote   = (LinearLayout) root.findViewById(R.id.llo_getting_start_note);
        mLloEnableNotificationNote  = (LinearLayout) root.findViewById(R.id.llo_enable_notice_note);
        mTvHeader           = (TextView) root.findViewById(R.id.tv_home_title);
        mTvTime             = (TextView) root.findViewById(R.id.tv_home_time);
        mTvOwnerNickName    = (TextView) root.findViewById(R.id.tv_owner_profile_nickname);
        mTvOwnerName        = (TextView) root.findViewById(R.id.tv_home_owner_name);
        mTvOwnerEmail       = (TextView) root.findViewById(R.id.tv_home_owner_email);
        mTvThreatNickName    = (TextView) root.findViewById(R.id.tv_threat_profile_nickname);
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
        mBtnFindTarget      = (Button) root.findViewById(R.id.btn_find_target);
        mBtnEnableNotification = (Button) root.findViewById(R.id.btn_enable_notice);
        mTvSuccessCaption   = (TextView) root.findViewById(R.id.tv_home_success);
        mTvFailedCaption    = (TextView) root.findViewById(R.id.tv_home_failed);
        mTvDefeatCaption    = (TextView) root.findViewById(R.id.tv_home_defeat);

        // Initiate
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                mTvOwnerName, mTvOwnerEmail, mBtnOwnerPosition,
                mTvThreatName, mTvThreatMatch, mTvThreatNote, mTvNote,
                mBtnOwnerPosition, mBtnMatchShares, mBtnAcceptDefeat, mTvThreatNickName,
                mTvSuccessCaption, mTvFailedCaption, mTvDefeatCaption);
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Bold,
                mTvSuccessValue, mTvFailedValue, mTvDefeatValue);
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Light, mTvHeader, mTvTime);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mTvThreatNote.setText(Html.fromHtml(getString(R.string.home_threat_content),
                    Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE));
        } else {
            mTvThreatNote.setText(Html.fromHtml(getString(R.string.home_threat_content)));
        }

        if(AppPreference.getInstance().getCurrentUserPersistence() != null) {
            AppPreference.getInstance().getSession().getActiveUser().userNoticeId =
                    AppPreference.getInstance().getCurrentUserPersistence().noticeId;
        }
        updateView();

        // Event Handler
        mBtnOwnerPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainMenuActivity) HomeFragment.this.getActivity()).showAccountSettingFragment();
            }
        });
        mBtnFindTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainMenuActivity) HomeFragment.this.getActivity())
                        .goToTab(MainMenuActivity.FRAGMENT_TARGETS_INDEX);

                EventBus.getInstance().post(new UpdateUserNoticeIdEvent(UserModel.NOTICE_ENABLE_PUSH_NOTIFICATION));
            }
        });
        mBtnEnableNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getInstance().post(new CheckNotificationEnableEvent());
            }
        });
        mBtnMatchShares.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getInstance().post(new MatchBuyoutEvent());
            }
        });
        mBtnAcceptDefeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getInstance().post(new FailMatchBuyoutEvent());
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

    @Override
    public void updateView() {
        EventBus.getInstance().post(new SetHomeStateEvent());
    }

    @Override
    public void handleNotificationEnable(boolean isEnabled) {
        mNotificationEnableDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mIsTimerRunning) {
            stopTimer(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateView();

        if(mIsTimerRunning) {
            startTimer();
        }
    }

    /*
    Event Bus
     */
    @Subscribe
    public void onMatchBuyOutCompleted(MatchBuyoutCompletedEvent event) {
        UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();

        if(event.getResult() == MatchBuyoutCompletedEvent.Result.Failed
                && activeUser.activeInboundBuyout != null) {
            Date issueDate = null;

            if (activeUser.activeInboundBuyout.deadlineAt == null) {
                Calendar c = Calendar.getInstance();
                c.setTime(activeUser.activeInboundBuyout.initiatedAt);
                c.add(Calendar.DATE, NUMBER_OF_DAY_TO_DEADLINE);
                issueDate = c.getTime();
            } else {
                issueDate = activeUser.activeInboundBuyout.deadlineAt;
            }

            Date currentTime = Calendar.getInstance().getTime();
            long elapseTime = issueDate.getTime() - currentTime.getTime();

            if (mState == State.Threat && elapseTime < 0L) {
                EventBus.getInstance().post(new FailMatchBuyoutEvent());
            } else {
                EventBus.getInstance().post(new SetHomeStateEvent());
            }
        } else {
            EventBus.getInstance().post(new SetHomeStateEvent());
        }
    }

    @Subscribe
    public void onUpdateHomeView(UpdateHomeViewEvent event) {
        changeState(event.getHomeState(), event.getNoticeId());
    }
}
