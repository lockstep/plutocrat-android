package com.whitefly.plutocrat.mainmenu.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
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
import android.text.format.DateUtils;
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
import com.whitefly.plutocrat.R;
import com.whitefly.plutocrat.helpers.AppPreference;
import com.whitefly.plutocrat.helpers.EventBus;
import com.whitefly.plutocrat.helpers.text.CustomTypefaceSpan;
import com.whitefly.plutocrat.mainmenu.MainMenuActivity;
import com.whitefly.plutocrat.mainmenu.events.CheckNotificationEnableEvent;
import com.whitefly.plutocrat.mainmenu.events.FailMatchBuyoutEvent;
import com.whitefly.plutocrat.mainmenu.events.MatchBuyoutEvent;
import com.whitefly.plutocrat.mainmenu.events.SetHomeStateEvent;
import com.whitefly.plutocrat.mainmenu.events.UpdateUserNoticeIdEvent;
import com.whitefly.plutocrat.mainmenu.views.IHomeView;
import com.whitefly.plutocrat.mainmenu.views.ITabView;
import com.whitefly.plutocrat.models.BuyoutModel;
import com.whitefly.plutocrat.models.TargetModel;
import com.whitefly.plutocrat.models.UserModel;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by Satjapot on 5/10/16 AD.
 */
public class HomeFragment extends Fragment implements ITabView, IHomeView {
    public static final String TITLE = "Home";
    public static final long NO_DELAY = 0L;
    public static final long ONE_SECOND = 1000L;

    public enum State {
        Default, Threat, Suspend
    }

    // Attributes
    private State mState;
    private String mHeaderDefault, mHeaderThreat, mHeaderSuspend;
    private String mNoteSuspend, mNoteDefault;
    private Drawable mDefaultBG, mThreatBG;
    private ClickableSpan mClickToBuyouts;
    private StyleSpan mStyleLink;
    private AlertDialog mNotificationEnableDialog;

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

    @Override
    public void changeState(State state, int noticeId) {
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
                        Spanned spannedText = Html.fromHtml(String.format(mNoteDefault, 4));
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
                mTvTime.setText("Deadline: 3d 14h 11s");
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
                mTvTime.setText("3d 14h 11s");
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
        Glide.with(getActivity()).load(getString(R.string.api_host) + activeUser.profileImage)
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
                .into(mImvOwnerPic);
        mIssueDate = activeUser.registeredAt;
    }

    private void setInitiatingUserView() {
        TargetModel initiatingUser = AppPreference.getInstance().getSession().getInitiatingUser();
        UserModel activeUser = AppPreference.getInstance().getSession().getActiveUser();
        String matchShare  = String.format(getString(R.string.caption_share_to_match),
                activeUser.activeInboundBuyout.numShares);

        mTvThreatNickName.setText(initiatingUser.getNickName());
        mTvThreatName.setText(initiatingUser.name);
        mTvThreatMatch.setText(matchShare);
        Glide.with(getActivity()).load(getString(R.string.api_host) + initiatingUser.profileImage)
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
            return String.format("%dh %dm %ds", hr, min, sec);
        } else {
            return String.format("%dd %dh %dm %ds", day, hr, min, sec);
        }
    }

    private void startTimer() {
        stopTimer();

        if(mIssueDate != null) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Date currentTime = Calendar.getInstance().getTime();
                            long elapseTime = Math.abs(mIssueDate.getTime() - currentTime.getTime());

                            mTvTime.setText(getTimeDurationString(elapseTime));
                        }
                    });
                }
            }, NO_DELAY, ONE_SECOND);
            mIsTimerRunning = true;
        }
    }

    private void stopTimer() {
        if(mTimer != null) {
            mTimer.cancel();
            mIsTimerRunning = false;
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

        // Initiate
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Regular,
                mTvOwnerName, mTvOwnerEmail, mBtnOwnerPosition,
                mTvThreatName, mTvThreatMatch, mTvThreatNote, mTvNote,
                mBtnOwnerPosition, mBtnMatchShares, mBtnAcceptDefeat, mTvThreatNickName,
                (TextView) root.findViewById(R.id.tv_home_success),
                (TextView) root.findViewById(R.id.tv_home_failed),
                (TextView) root.findViewById(R.id.tv_home_defeat));
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Bold,
                mTvSuccessValue, mTvFailedValue, mTvDefeatValue);
        AppPreference.getInstance().setFontsToViews(AppPreference.FontType.Light, mTvHeader, mTvTime);

        mTvThreatNote.setText(Html.fromHtml(getString(R.string.home_threat_content)));

        // TODO: Delete Debug code
        AppPreference.getInstance().getSession().getActiveUser().userNoticeId = UserModel.NOTICE_GETTING_STARTED;

        EventBus.getInstance().post(new SetHomeStateEvent());

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

    }

    @Override
    public void handleNotificationEnable(boolean isEnabled) {
        mNotificationEnableDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mIsTimerRunning) {
            stopTimer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mIsTimerRunning) {
            startTimer();
        }
    }
}
