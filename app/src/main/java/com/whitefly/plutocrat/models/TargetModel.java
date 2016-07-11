package com.whitefly.plutocrat.models;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Satjapot on 5/17/16 AD.
 */
public class TargetModel {
    public static final int DEBUG_NO_PROFILE_PICTURE = 0;

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

    @SerializedName("under_buyout_threat")
    public boolean isUnderBuyoutThreat;

    @SerializedName("defeated_at")
    public Date defeatedAt;

    @SerializedName("is_plutocrat")
    public boolean isPlutocrat;

    // Methods
    public String getNickName() {
        return nickName;
    }

    public long getDaySurvived() {
        long result = 0L;
        if(registeredAt != null) {
            Date currentTime = Calendar.getInstance().getTime();
            long elapseTime = Math.abs(registeredAt.getTime() - currentTime.getTime());
            result = TimeUnit.MILLISECONDS.toDays(elapseTime);
        }
        return result;
    }
}
