package com.whitefly.plutocrat.models;

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
    public String display_name;
    public String profile_image_url;
    public Date registered_at;
    public boolean attacking_current_user;
    public int successful_buyouts_count;
    public int matched_buyouts_count;
    public int available_shares_count;
    public boolean under_buyout_threat;
    public Date defeated_at;
    public boolean is_plutocrat;
    public String active_inbound_buyout;
    public String terminal_buyout;
    public String email;
    public boolean transactional_emails_enabled;
    public boolean product_emails_enabled;
    public int failed_buyouts_count;
    public int user_notice_id;

    public boolean is_enable_notification;



    // Methods
    public String getNickName() {
        String result;
        char firstCh, secondCh;

        if(display_name == null) {
            result = "";
        } else {
            String[] names = display_name.split("\\s+");
            if(names.length > 1) {
                firstCh = names[0].charAt(0);
                secondCh = names[1].charAt(0);
                result = String.format("%s%s", firstCh, secondCh).toUpperCase();
            } else {
                firstCh = display_name.charAt(0);
                secondCh = display_name.length() > 1 ? display_name.charAt(1) : Character.MIN_VALUE;
                result = String.format("%s%s", firstCh, secondCh).toUpperCase();
            }
        }

        return result;
    }

}
