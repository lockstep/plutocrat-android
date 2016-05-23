package com.whitefly.plutocrat.models;

import java.util.Date;

/**
 * Created by Satjapot on 5/9/16 AD.
 */
public class UserModel {
    // Attributes
    public int id;
    public String display_name;
    public String profile_image_url;
    public Date registered_at;
    public boolean attacking_current_user;
    public int successful_buyouts_count;
    public int matched_buyouts_count;
    public boolean under_buyout_threat;
    public Date defeated_at;
    public boolean is_plutocrat;

}
