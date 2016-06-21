package com.whitefly.plutocrat.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Satjapot on 5/9/16 AD.
 */
public class UserModel {
    public static final int NOTICE_DEFAULT = 0;
    public static final int NOTICE_GETTING_STARTED = 1;
    public static final int NOTICE_ENABLE_PUSH_NOTIFICATION = 2;

    // Attributes
    public int id;

    @SerializedName("display_name")
    public String name;

    @SerializedName("initials")
    public String nickName;

    @SerializedName("profile_image_url")
    public String profileImage;

    @SerializedName("registered_at")
    public Date registeredAt;

    @SerializedName("attacking_current_user")
    public boolean isAttackingCurrentUser;

    @SerializedName("successful_buyouts_count")
    public int numSuccessfulBuyout;

    @SerializedName("matched_buyouts_count")
    public int numMatchedBuyout;

    @SerializedName("available_shares_count")
    public int numAvailableShares;

    @SerializedName("buyouts_until_plutocrat_count")
    public int numBuyoutUntilPlutocrat;

    @SerializedName("under_buyout_threat")
    public boolean isUnderBuyoutThreat;

    @SerializedName("defeated_at")
    public Date defeatedAt;

    @SerializedName("is_plutocrat")
    public boolean isPlutocrat;

    public transient BuyoutModel activeInboundBuyout;

    public transient BuyoutModel terminalBuyout;

    public String email;

    @SerializedName("transactional_emails_enabled")
    public boolean isTransactionalEmailsEnabled;

    @SerializedName("product_emails_enabled")
    public boolean isProductEmailsEnabled;

    @SerializedName("failed_buyouts_count")
    public int numFailedBuyouts;

    @SerializedName("user_notice_id")
    public int userNoticeId;

    @SerializedName("is_enable_notification")
    public boolean isEnableNotification;

    // Methods
    public String getNickName() {

        return nickName;
    }

    public void updateTargetData(TargetModel model) {;
        name = model.name;
        nickName = model.nickName;
        profileImage = model.profileImage;
        isAttackingCurrentUser = model.isAttackingCurrentUser;
        numSuccessfulBuyout = model.numSuccessfulBuyout;
        numMatchedBuyout = model.numMatchedBuyout;
        numAvailableShares = model.numAvailableShares;
        isUnderBuyoutThreat = model.isUnderBuyoutThreat;
        defeatedAt = model.defeatedAt;
        isPlutocrat = model.isPlutocrat;
    }
}
