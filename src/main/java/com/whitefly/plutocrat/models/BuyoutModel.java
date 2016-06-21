package com.whitefly.plutocrat.models;

import com.google.gson.annotations.SerializedName;
import com.whitefly.plutocrat.helpers.AppPreference;

import java.util.Date;

/**
 * Created by Satjapot on 5/17/16 AD.
 */
public class BuyoutModel {
    public static final int DEBUG_NO_PROFILE_PICTURE = 0;

    public enum BuyoutStatus {
        Initiate, Threat
    }

    public enum GameStatus {
        Initiated, Matched, Success
    }

    // Attributes
    public int id;

    @SerializedName("initiated_at")
    public Date initiatedAt;

    @SerializedName("initiated_time_ago")
    public String initiatedTimeAgo;

    @SerializedName("deadline_at")
    public Date deadlineAt;

    @SerializedName("number_of_shares")
    public int numShares;

    public String state;

    @SerializedName("resolved_at")
    public String resolvedAt;

    @SerializedName("resolved_time_ago")
    public String resolvedTimeAgo;

    @SerializedName("target_user_id")
    public int targetUserId;

    @SerializedName("initiating_user_id")
    public int initiatingUserId;

    @SerializedName("created_at")
    public Date createdAt;

    @SerializedName("updated_at")
    public Date updatedAt;

    public transient TargetModel initiatingUser;
    public transient TargetModel targetUser;

    // Method
    public String getTimeAgo() {
        if(resolvedTimeAgo != null) {
            return resolvedTimeAgo;
        }
        return initiatedTimeAgo;
    }

    public BuyoutStatus getBuyoutStatus() {
        BuyoutStatus result = BuyoutStatus.Initiate;
        UserModel currentUser = AppPreference.getInstance().getSession().getActiveUser();
        if(currentUser.id == initiatingUser.id) {
            result = BuyoutStatus.Initiate;
        } else if(currentUser.id == targetUser.id) {
            result = BuyoutStatus.Threat;
        }

        return result;
    }

    public GameStatus getGameStatus() {
        if(state.equals("initiated")) {
            return GameStatus.Initiated;
        } else if(state.equals("matched") || state.equals("failed")) {
            return GameStatus.Matched;
        }else {
            return GameStatus.Success;
        }
    }
}
