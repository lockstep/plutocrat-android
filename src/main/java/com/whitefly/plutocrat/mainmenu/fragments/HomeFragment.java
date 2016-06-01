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
import com.whitefly.plutocrat.mainmenu.events.SetHomeStateEvent;
import com.whitefly.plutocrat.mainmenu.events.UpdateUserNoticeIdEvent;
import com.whitefly.plutocrat.mainmenu.views.IHomeView;
import com.whitefly.plutocrat.mainmenu.views.ITabView;
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
    private Drawable mDefaultBG, mThreatBG, mTopLineBG, mBottomLineBG;
    private ClickableSpan mClickToBuyouts;
    private StyleSpan mStyleLink;
    private AlertDialog mNotificationEnableDialog;

    private Timer mTimer;
    private Date mIssueDate;

    // Views
    private LinearLayout mLloThreat, mLloOwner, mLloNote, mLloHeader;
    private LinearLayout mLloActiveDefaultNote, mLloFindTargetDefaultNote, mLloEnableNotificationNote;
    private RelativeLayout mLloShares;
    private TextView mTvHeader, mTvTime;
    private TextView mTvOwnerNickName, mTvOwnerName, mTvOwnerEmail;
    private TextView mTvThreatName, mTvThreatMatch, mTvThreatNote;
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

    private void setOwnerView() {
        UserModel model = AppPreference.getInstance().getSession().getActiveUser();
        mTvOwnerNickName.setText(model.getNickName());
        mTvOwnerName.setText(model.display_name);
        mTvOwnerEmail.setText(model.email);
        mTvSuccessValue.setText(String.valueOf(model.successful_buyouts_count));
        mTvFailedValue.setText(String.valueOf(model.failed_buyouts_count));
        mTvDefeatValue.setText(String.valueOf(model.matched_buyouts_count));
        Glide.with(getActivity()).load(getString(R.string.api_host) + model.profile_image_url)
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
        mIssueDate = model.registered_at;
        startTimer();
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

                            long day = TimeUnit.MILLISECONDS.toDays(elapseTime);
                            long hr = TimeUnit.MILLISECONDS.toHours(elapseTime - TimeUnit.DAYS.toMillis(day));
                            long min = TimeUnit.MILLISECONDS.toMinutes(elapseTime - TimeUnit.DAYS.toMillis(day) - TimeUnit.HOURS.toMillis(hr));
                            long sec = TimeUnit.MILLISECONDS.toSeconds(elapseTime - TimeUnit.DAYS.toMillis(day) - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));

                            mTvTime.setText(String.format("%dd %dh %dm %ds", day, hr, min, sec));
                        }
                    });
                }
            }, NO_DELAY, ONE_SECOND);

        }
    }

    private void stopTimer() {
        if(mTimer != null) {
            mTimer.cancel();
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
        mTopLineBG      = ContextCompat.getDrawable(getActivity(), R.drawable.bg_line_top);
        mBottomLineBG   = ContextCompat.getDrawable(getActivity(), R.drawable.bg_line_bottom);

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
        if(mTimer == null) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {

                }
            }, NO_DELAY, ONE_SECOND);
        }
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
        mLloFindTargetDefaultNote   = (LinearLayout) root.findViewById(R.id.llo_getting_start_note);
        mLloEnableNotificationNote  = (LinearLayout) root.findViewById(R.id.llo_enable_notice_note);
        mTvHeader           = (TextView) root.findViewById(R.id.tv_home_title);
        mTvTime             = (TextView) root.findViewById(R.id.tv_home_time);
        mTvOwnerNickName    = (TextView) root.findViewById(R.id.tv_owner_profile_nickname);
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
        mBtnFindTarget      = (Button) root.findViewById(R.id.btn_find_target);
        mBtnEnableNotification = (Button) root.findViewById(R.id.btn_enable_notice);

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

        // TODO: Delete Debug code
        AppPreference.getInstance().getSession().getActiveUser().user_notice_id = UserModel.NOTICE_GETTING_STARTED;

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
                // TODO: Push notification permission show
                EventBus.getInstance().post(new CheckNotificationEnableEvent());
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
    public void handleNotificationEnable(boolean isEnabled) {
        mNotificationEnableDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        startTimer();
    }
}
